package de.stoupas.xkcd.comic

data class ComicUiState(
    val comic: Comic? = null,
    val loading: Boolean = true,
    val failure: ComicFailure? = null,
    val resolvedImageUrl: String? = null,
    val settingsWriteFailed: Boolean = false,
)
