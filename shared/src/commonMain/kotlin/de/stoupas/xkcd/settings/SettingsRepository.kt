package de.stoupas.xkcd.settings

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class SettingsRepository(private val storage: SettingsStorage) {
    private val mutableState = MutableStateFlow(UserSettings())
    val state: StateFlow<UserSettings> = mutableState.asStateFlow()
    private val mutex = Mutex()
    private var initialized = false
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun initialize(legacy: LegacySettings): Result<Unit> = mutex.withLock {
        attempt {
            if (initialized) return@attempt
            val previous = storage.read()?.let { source ->
                val fields = json.parseToJsonElement(source).jsonObject
                for (key in listOf("darkTheme", "dataSaver")) {
                    val value = fields[key]
                    require(value == null || value == JsonNull ||
                        value is JsonPrimitive && !value.isString && value.content in listOf("true", "false")) {
                        "Invalid settings field: $key"
                    }
                }
                json.decodeFromString<Snapshot>(source)
            } ?: Snapshot()
            require(previous.schemaVersion == 1) { "Unsupported settings schema" }
            require(previous.migrationVersion in 0..1) { "Unsupported migration version" }
            val import = if (previous.migrationVersion == 0) legacy else LegacySettings()
            val merged = UserSettings(
                darkTheme = previous.darkTheme ?: import.darkTheme ?: true,
                accentArgb = (previous.accentArgb ?: import.accentArgb)?.takeIf { it in 0..0xFFFFFFFF } ?: 0xFF673AB7,
                dataSaver = previous.dataSaver ?: import.dataSaver ?: false,
                favorites = (previous.favorites ?: import.favorites ?: emptyList()).filter { it > 0 && it != 404 }.distinct(),
            )
            persist(merged)
            initialized = true
        }
    }

    suspend fun setTheme(dark: Boolean) = update { it.copy(darkTheme = dark) }
    suspend fun setAccent(argb: Long) = update {
        require(argb in 0..0xFFFFFFFF) { "Invalid accent color" }
        it.copy(accentArgb = argb)
    }
    suspend fun setDataSaver(enabled: Boolean) = update { it.copy(dataSaver = enabled) }
    suspend fun toggleFavorite(id: Int) = update {
        require(id > 0 && id != 404) { "Invalid comic number" }
        it.copy(favorites = if (id in it.favorites) it.favorites - id else it.favorites + id)
    }
    suspend fun removeFavorite(id: Int) = update { it.copy(favorites = it.favorites - id) }
    suspend fun clearFavorites() = update { it.copy(favorites = emptyList()) }

    private suspend fun update(transform: (UserSettings) -> UserSettings): Result<Unit> = mutex.withLock {
        attempt {
            check(initialized) { "Settings import must complete before changes are allowed" }
            persist(transform(mutableState.value))
        }
    }

    private suspend fun persist(settings: UserSettings) {
        val snapshot = Snapshot(1, 1, settings.darkTheme, settings.accentArgb, settings.dataSaver, settings.favorites)
        storage.write(json.encodeToString(snapshot)).getOrThrow()
        mutableState.value = settings
    }

    private suspend fun attempt(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Result.failure(failure)
    }

    @Serializable
    private data class Snapshot(
        val schemaVersion: Int = 1,
        val migrationVersion: Int = 0,
        val darkTheme: Boolean? = null,
        val accentArgb: Long? = null,
        val dataSaver: Boolean? = null,
        val favorites: List<Int>? = null,
    )
}
