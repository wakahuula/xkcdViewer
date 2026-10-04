package de.stoupas.xkcd.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import de.stoupas.xkcd.app.AppPage
import de.stoupas.xkcd.platform.LicenseSource
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LicensesScreen(source: LicenseSource, onBack: () -> Unit) {
    var libraries by remember { mutableStateOf<Libs?>(null) }
    var bundled by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(source, attempt) {
        failed = false
        try {
            bundled = listOf("xkcdViewer" to "app_license.txt", "Fira Mono" to "fira_mono_license.txt").map { (name, file) ->
                name to Res.readBytes("files/$file").decodeToString()
            }
            libraries = Libs.Builder().withJson(source.readJson()).build()
        }
        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
    }
    AppPage(stringResource(Res.string.library_licenses), onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (failed) item { Button(onClick = { attempt++ }) { Text(stringResource(Res.string.retry)) } }
            else if (libraries == null) item { CircularProgressIndicator() }
            items(bundled, key = { it.first }) { (name, license) ->
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(license, style = MaterialTheme.typography.bodySmall)
            }
            items(libraries?.libraries.orEmpty(), key = { it.uniqueId }) { library ->
                Column {
                    Text(library.name, style = MaterialTheme.typography.titleMedium)
                    library.artifactVersion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    for (license in library.licenses) {
                        Text(license.name, style = MaterialTheme.typography.labelLarge)
                        license.licenseContent?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        license.url?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                    HorizontalDivider(Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}
