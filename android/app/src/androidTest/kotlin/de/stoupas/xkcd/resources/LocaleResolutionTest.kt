package de.stoupas.xkcd.resources

import androidx.test.platform.app.InstrumentationRegistry
import android.os.LocaleList
import android.os.Build
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleResolutionTest {
    @Test
    fun allNineLanguagesAndFallbacksResolveOnAndroid() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previous = Locale.getDefault()
        val config = android.content.res.Configuration(context.resources.configuration)
        try {
            for ((tag, expected) in listOf(
                "de-DE" to "Einstellungen", "en-US" to "Settings", "es-ES" to "Ajustes",
                "fr-FR" to "Paramètres", "hu-HU" to "Beállítások", "ja-JP" to "設定",
                "pt-BR" to "Configurações", "ru-RU" to "Настройки", "uk-UA" to "Налаштування",
                "de-AT" to "Einstellungen", "zz-ZZ" to "Settings",
            )) {
                val locale = Locale.forLanguageTag(tag)
                Locale.setDefault(locale)
                val changed = android.content.res.Configuration(config)
                changed.setLocale(locale)
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(changed, context.resources.displayMetrics)
                assertEquals(tag, expected, getString(Res.string.settings))
            }
        } finally {
            Locale.setDefault(previous)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}
