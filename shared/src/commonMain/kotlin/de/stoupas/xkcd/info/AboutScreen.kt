package de.stoupas.xkcd.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.stoupas.xkcd.app.*
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(onBack: () -> Unit, onContributors: () -> Unit, onLicenses: () -> Unit, onWebsite: (String) -> Unit) {
    AppPage(stringResource(Res.string.about_this_app), onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text(APP_NAME, style = MaterialTheme.typography.headlineLarge) }
            item { Text(stringResource(Res.string.built_by)) }
            item { Text(stringResource(Res.string.xkcd_license)) }
            item { TextButton(onClick = { onWebsite("https://xkcd.com/") }) { Text("xkcd.com") } }
            item { Text(stringResource(Res.string.launcher_icon)) }
            item { Text(stringResource(Res.string.app_license_notice)) }
            item { Text(stringResource(Res.string.font_license_notice)) }
            item { TextButton(onClick = onContributors) { Text(stringResource(Res.string.contributors_title)) } }
            item { TextButton(onClick = onLicenses) { Text(stringResource(Res.string.library_licenses)) } }
        }
    }
}
