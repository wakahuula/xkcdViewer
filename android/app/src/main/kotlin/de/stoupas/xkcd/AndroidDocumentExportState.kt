package de.stoupas.xkcd

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import de.stoupas.xkcd.platform.PlatformFailure
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Retained export state. Never stores an Activity or its launcher. */
class AndroidDocumentExportState(
    private val resolver: ContentResolver,
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private var pending: CompletableDeferred<Uri?>? = null
    private val mutableLaunchRequest = MutableStateFlow<Intent?>(null)
    val launchRequest = mutableLaunchRequest.asStateFlow()

    fun takeLaunchRequest(): Intent? = mutableLaunchRequest.value.also { mutableLaunchRequest.value = null }

    fun receiveResult(uri: Uri?) {
        val awaiting = pending
        pending = null
        if (awaiting == null || !awaiting.complete(uri)) {
            if (uri != null) scope.launch(Dispatchers.IO) { withContext(NonCancellable) { cleanup(uri) } }
        }
    }

    fun launchFailed(failure: Exception) {
        val awaiting = pending
        pending = null
        awaiting?.completeExceptionally(failure)
    }

    suspend fun export(bytes: ByteArray, mime: String, name: String): Result<Unit> = mutex.withLock {
        var uri: Uri? = null
        var awaiting: CompletableDeferred<Uri?>? = null
        try {
            withContext(Dispatchers.Main.immediate) {
                check(pending == null) { "An export dialog is already open" }
                awaiting = CompletableDeferred()
                pending = awaiting
                mutableLaunchRequest.value = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = mime
                    putExtra(Intent.EXTRA_TITLE, name)
                }
            }
            uri = awaiting!!.await() ?: return@withLock Result.failure(PlatformFailure.UserCancelled)
            withContext(Dispatchers.IO) {
                requireNotNull(resolver.openOutputStream(uri, "w")).use { it.write(bytes) }
                ensureActive()
            }
            Result.success(Unit)
        } catch (failure: Exception) {
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                // Cancellation can win await's dispatch after the result already arrived.
                if (uri == null && awaiting?.isCompleted == true && awaiting?.isCancelled == false) {
                    uri = awaiting!!.await()
                }
                awaiting?.cancel()
                // If the picker was not launched, no late result needs to be drained.
                if (awaiting != null && mutableLaunchRequest.value != null && pending === awaiting) {
                    mutableLaunchRequest.value = null
                    pending = null
                }
            }
            uri?.let { withContext(NonCancellable + Dispatchers.IO) { cleanup(it) } }
            if (failure is CancellationException) throw failure
            Result.failure(PlatformFailure.WriteFailed())
        }
    }

    private fun cleanup(uri: Uri) {
        try { DocumentsContract.deleteDocument(resolver, uri) }
        catch (_: Exception) { /* External providers may not support deletion. */ }
    }
}
