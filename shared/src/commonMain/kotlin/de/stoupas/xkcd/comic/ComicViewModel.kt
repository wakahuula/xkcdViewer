package de.stoupas.xkcd.comic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.stoupas.xkcd.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ComicViewModel(
    private val comics: ComicRepository,
    private val settings: SettingsRepository,
    private val savedState: SavedStateHandle? = null,
    initialComicId: Int? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ComicUiState())
    val state = mutableState.asStateFlow()
    private var requestJob: Job? = null
    private var generation = 0
    private var lastRequest: suspend () -> Result<Comic> = { comics.latest() }

    init {
        var dataSaver = settings.state.value.dataSaver
        viewModelScope.launch {
            settings.state.collect {
                if (it.dataSaver != dataSaver) {
                    dataSaver = it.dataSaver
                    mutableState.update { current -> current.copy(resolvedImageUrl = null) }
                }
            }
        }
        val restored = savedState?.get<Int>("comic_id") ?: initialComicId
        if (restored != null) select(restored) else loadLatest()
    }

    fun loadLatest() = request { comics.latest() }
    fun select(id: Int) = request { comics.byId(id) }
    fun loadRandom() = request { comics.random(Random.Default) }
    fun loadAdjacent(delta: Int) {
        val current = state.value.comic ?: return
        request {
            val latest = comics.latestKnownId ?: comics.latest().getOrElse { return@request Result.failure(it) }.num
            comics.byId(adjacentId(current.num, delta, latest))
        }
    }
    fun retry() = request(lastRequest)

    fun resolvedImage(url: String) {
        val current = state.value
        val comic = current.comic ?: return
        if (!current.loading && url in imageCandidates(comic, settings.state.value.dataSaver)) {
            mutableState.update { it.copy(resolvedImageUrl = url) }
        }
    }

    fun toggleFavorite() {
        val current = state.value
        if (current.loading) return
        val comic = current.comic ?: return
        viewModelScope.launch {
            val result = settings.toggleFavorite(comic.num)
            mutableState.update { it.copy(settingsWriteFailed = result.isFailure) }
        }
    }

    private fun request(load: suspend () -> Result<Comic>) {
        val identity = ++generation
        requestJob?.cancel()
        lastRequest = load
        mutableState.update { it.copy(loading = true, failure = null, resolvedImageUrl = null) }
        requestJob = viewModelScope.launch {
            try {
                val result = load()
                if (identity != generation) return@launch
                result.fold(
                    onSuccess = { comic ->
                        savedState?.set("comic_id", comic.num)
                        mutableState.value = ComicUiState(comic = comic, loading = false)
                    },
                    onFailure = { error -> mutableState.update { it.copy(loading = false, failure = error as? ComicFailure ?: ComicFailure.Network()) } },
                )
            } catch (cancelled: CancellationException) {
                if (identity == generation) mutableState.update { it.copy(loading = false) }
                throw cancelled
            }
        }
    }
}
