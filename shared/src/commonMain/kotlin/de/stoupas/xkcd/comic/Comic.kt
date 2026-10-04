package de.stoupas.xkcd.comic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comic(
    val num: Int,
    val title: String,
    val alt: String,
    val img: String,
    val year: String,
    val month: String,
    val day: String,
    @SerialName("safe_title") val safeTitle: String = title,
    val link: String = "",
    val news: String = "",
    val transcript: String = "",
)
