package de.stoupas.xkcd.resources

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Contributor(val name: String, val profile: String)

@Serializable
private data class ContributorList(val contributors: List<Contributor>)

private val contributorJson = Json { ignoreUnknownKeys = true }

fun parseContributors(json: String): List<Contributor> =
    contributorJson.decodeFromString<ContributorList>(json).contributors

suspend fun loadContributors(): List<Contributor> =
    parseContributors(Res.readBytes("files/contributors.json").decodeToString())
