package de.stoupas.xkcd.settings

interface SettingsStorage {
    fun read(): String?
    suspend fun write(json: String): Result<Unit>
}
