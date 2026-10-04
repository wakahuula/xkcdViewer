package de.stoupas.xkcd

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import de.stoupas.xkcd.platform.PlatformFailure
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidDocumentExporter(private val activity: ComponentActivity) {
    private val mutex = Mutex()
    private var pending: CompletableDeferred<Uri?>? = null
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = if (result.resultCode == Activity.RESULT_OK) result.data?.data else null
        val awaiting = pending
        pending = null
        if (awaiting == null || awaiting.isCancelled) {
            if (uri != null) activity.lifecycleScope.launch(Dispatchers.IO) { withContext(NonCancellable) { cleanup(uri) } }
        } else awaiting.complete(uri)
    }

    suspend fun export(bytes: ByteArray, mime: String, name: String): Result<Unit> = mutex.withLock {
        var uri: Uri? = null
        try {
            check(pending == null) { "An export dialog is already open" }
            val result = CompletableDeferred<Uri?>()
            withContext(Dispatchers.Main) {
                pending = result
                launcher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = mime
                    putExtra(Intent.EXTRA_TITLE, name)
                })
            }
            uri = result.await() ?: return@withLock Result.failure(PlatformFailure.UserCancelled)
            withContext(Dispatchers.IO) {
                requireNotNull(activity.contentResolver.openOutputStream(uri, "w")).use { it.write(bytes) }
            }
            Result.success(Unit)
        } catch (failure: Exception) {
            if (failure is CancellationException) pending?.cancel()
            else pending = null
            uri?.let { withContext(NonCancellable + Dispatchers.IO) { cleanup(it) } }
            if (failure is CancellationException) throw failure
            Result.failure(PlatformFailure.WriteFailed())
        }
    }

    private fun cleanup(uri: Uri) { try { activity.contentResolver.delete(uri, null, null) } catch (_: Exception) { /* best effort for external providers */ } }
}
