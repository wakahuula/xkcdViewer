package de.stoupas.xkcd.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.stoupas.xkcd.comic.*
import de.stoupas.xkcd.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class FavoriteItem(val id: Int, val comic: Comic? = null, val failure: ComicFailure? = null)

class FavoritesViewModel(private val comics: ComicRepository, private val settings: SettingsRepository) : ViewModel() {
    private val mutableItems = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val items = mutableItems.asStateFlow()
    private val mutableLoading = MutableStateFlow(false)
    val loading = mutableLoading.asStateFlow()
    private val jobs = mutableMapOf<Int, Job>()
    private val semaphore = Semaphore(4)

    init {
        viewModelScope.launch {
            settings.state.collect { current ->
                val known = mutableItems.value.associateBy { it.id }
                val removed = jobs.keys.filter { it !in current.favorites }
                removed.forEach { jobs.remove(it)?.cancel() }
                mutableItems.value = current.favorites.map { known[it] ?: FavoriteItem(it) }
                current.favorites.filter { it !in known }.forEach(::load)
                refreshLoading()
            }
        }
    }

    fun retry(id: Int) {
        if (id in settings.state.value.favorites && jobs[id]?.isActive != true) load(id)
    }

    private fun load(id: Int) {
        jobs[id] = viewModelScope.launch {
            try {
                val result = semaphore.withPermit { comics.byId(id) }
                mutableItems.value = mutableItems.value.map { item ->
                    if (item.id == id) FavoriteItem(id, result.getOrNull(), result.exceptionOrNull()?.let { it as? ComicFailure ?: ComicFailure.Network() }) else item
                }
            } finally {
                jobs.remove(id)
                refreshLoading()
            }
        }
        refreshLoading()
    }

    private fun refreshLoading() { mutableLoading.value = jobs.isNotEmpty() }
}
