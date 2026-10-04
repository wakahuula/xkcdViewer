package de.stoupas.xkcd.settings

data class LegacySettings(
    val darkTheme: Boolean? = null,
    val accentArgb: Long? = null,
    val dataSaver: Boolean? = null,
    val favorites: List<Int>? = null,
)
