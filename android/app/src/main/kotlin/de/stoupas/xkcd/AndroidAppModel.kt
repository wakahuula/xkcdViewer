package de.stoupas.xkcd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.stoupas.xkcd.comic.ComicRepository
import de.stoupas.xkcd.comic.KtorComicApi
import de.stoupas.xkcd.settings.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StartupState { Loading, Ready, Failed }

class AndroidAppModel(application: Application) : AndroidViewModel(application) {
    val http = HttpClient(OkHttp)
    private val api = KtorComicApi(http)
    val comics = ComicRepository(api)
    val settings = SettingsRepository(AndroidSettingsStorage(application))
    private val mutableStartup = MutableStateFlow(StartupState.Loading)
    val startup = mutableStartup.asStateFlow()
    private var initialization: Job? = null

    init { retryImport() }

    fun retryImport() {
        if (initialization?.isActive == true) return
        mutableStartup.value = StartupState.Loading
        initialization = viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try { settings.initialize(FlutterPreferencesReader(getApplication<Application>()).read()) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) { Result.failure(failure) }
            }
            mutableStartup.value = if (result.isSuccess) StartupState.Ready else StartupState.Failed
        }
    }

    override fun onCleared() { api.close(); http.close() }
}
