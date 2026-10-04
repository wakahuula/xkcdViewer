package de.stoupas.xkcd.settings

import android.content.Context
import android.content.SharedPreferences

class FlutterPreferencesReader(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE))

    fun read(): LegacySettings {
        val values = preferences.all
        val favorites = when (val stored = values["flutter.favorites"]) {
            is String -> LegacyStringListDecoder.decode(stored)?.mapNotNull { it.toIntOrNull() }
            is Set<*> -> stored.filterIsInstance<String>().mapNotNull { it.toIntOrNull() }.sorted()
            else -> null
        }
        return LegacySettings(
            darkTheme = values["flutter.isDark"] as? Boolean,
            accentArgb = (values["flutter.accentColor"] as? Long)?.takeIf { it in 0..0xFFFFFFFF },
            dataSaver = values["flutter.data_saver"] as? Boolean,
            favorites = favorites,
        )
    }
}
