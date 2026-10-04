package de.stoupas.xkcd.app

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Favorites : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data object Contributors : AppRoute
    @Serializable data object About : AppRoute
    @Serializable data object Licenses : AppRoute
    @Serializable data class ComicDetail(val id: Int) : AppRoute
}
