package de.stoupas.xkcd.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val darkTheme: Boolean = true,
    val accentArgb: Long = 0xFF673AB7,
    val dataSaver: Boolean = false,
    val favorites: List<Int> = emptyList(),
)
