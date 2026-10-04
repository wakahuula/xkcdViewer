package de.stoupas.xkcd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.stoupas.xkcd.app.XkcdApp
import de.stoupas.xkcd.resources.settings_import_failed
import de.stoupas.xkcd.resources.retry
import de.stoupas.xkcd.settings.AndroidSettingsStorage
import de.stoupas.xkcd.settings.FlutterPreferencesReader
import de.stoupas.xkcd.settings.SettingsRepository
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        coil3.SingletonImageLoader.setSafe { de.stoupas.xkcd.platform.createAndroidImageLoader(it) }
        val settings = SettingsRepository(AndroidSettingsStorage(applicationContext))
        setContent {
            var attempt by remember { mutableIntStateOf(0) }
            var ready by remember { mutableStateOf(false) }
            var failed by remember { mutableStateOf(false) }
            LaunchedEffect(attempt) {
                failed = false
                val result = withContext(Dispatchers.IO) {
                    settings.initialize(FlutterPreferencesReader(applicationContext).read())
                }
                ready = result.isSuccess
                failed = result.isFailure
            }
            if (ready) {
                XkcdApp()
            } else {
                MaterialTheme {
                    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                        if (failed) {
                            Text(org.jetbrains.compose.resources.stringResource(de.stoupas.xkcd.resources.Res.string.settings_import_failed))
                            Button(onClick = { attempt++ }) {
                                Text(org.jetbrains.compose.resources.stringResource(de.stoupas.xkcd.resources.Res.string.retry))
                            }
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
