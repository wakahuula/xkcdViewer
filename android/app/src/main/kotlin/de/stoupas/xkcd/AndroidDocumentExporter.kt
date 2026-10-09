package de.stoupas.xkcd

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/** Activity-local bridge; outstanding data and writes belong to the retained model. */
class AndroidDocumentExporter(activity: ComponentActivity) {
    private val state = ViewModelProvider(activity)[AndroidAppModel::class.java].documents
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        state.receiveResult(if (result.resultCode == Activity.RESULT_OK) result.data?.data else null)
    }

    init {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                state.launchRequest.collect {
                    val request = state.takeLaunchRequest() ?: return@collect
                    try { launcher.launch(request) }
                    catch (failure: Exception) { state.launchFailed(failure) }
                }
            }
        }
    }

    suspend fun export(bytes: ByteArray, mime: String, name: String): Result<Unit> = state.export(bytes, mime, name)
}
