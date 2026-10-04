package de.stoupas.xkcd.settings

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidSettingsStorage(
    private val preferences: SharedPreferences,
    private val commit: (SharedPreferences.Editor) -> Boolean = { it.commit() },
) : SettingsStorage {
    constructor(context: Context) : this(context.getSharedPreferences("xkcd_settings", Context.MODE_PRIVATE))

    private val settings = SharedPreferencesSettings(preferences)
    override fun read(): String? = settings.getStringOrNull("settings_snapshot")

    override suspend fun write(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            check(commit(preferences.edit().putString("settings_snapshot", json))) { "Could not persist settings" }
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
}
