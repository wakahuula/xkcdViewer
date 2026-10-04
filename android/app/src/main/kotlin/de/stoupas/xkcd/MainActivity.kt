package de.stoupas.xkcd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import de.stoupas.xkcd.app.AppDependencies
import de.stoupas.xkcd.app.XkcdApp
import de.stoupas.xkcd.platform.*
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

class MainActivity : ComponentActivity() {
    private var imageSaver: AndroidImageSaveService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SingletonImageLoader.setSafe { createAndroidImageLoader(it) }
        val model = ViewModelProvider(this)[AndroidAppModel::class.java]
        val exporter = AndroidDocumentExporter(this)
        val saver = AndroidImageSaveService(this, model.http, exportDocument = exporter::export)
        imageSaver = saver
        val dependencies = AppDependencies(
            model.comics, model.settings, AndroidShareService(this), saver, AndroidBrowserService(this),
            AndroidImageCacheService(SingletonImageLoader.get(this)), AndroidLicenseSource(this, R.raw.aboutlibraries),
        )
        setContent {
            val startup by model.startup.collectAsStateWithLifecycle()
            val settings by model.settings.state.collectAsStateWithLifecycle()
            SideEffect { updateSystemBars(settings.darkTheme) }
            if (startup == StartupState.Ready) XkcdApp(dependencies)
            else MaterialTheme {
                Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                    if (startup == StartupState.Failed) {
                        Text(stringResource(Res.string.settings_import_failed))
                        Button(onClick = model::retryImport) { Text(stringResource(Res.string.retry)) }
                    } else CircularProgressIndicator()
                }
            }
        }
    }

    internal fun updateSystemBars(dark: Boolean) {
        val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    override fun onDestroy() {
        imageSaver?.close()
        super.onDestroy()
    }
}
