package de.stoupas.xkcd.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.stoupas.xkcd.app.AppPage
import de.stoupas.xkcd.platform.*
import de.stoupas.xkcd.resources.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(repository: SettingsRepository, cache: ImageCacheService, onBack: () -> Unit, onAbout: () -> Unit, onContributors: () -> Unit, onLicenses: () -> Unit, onError: () -> Unit) {
    val settings by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var accentDialog by remember { mutableStateOf(false) }
    var cacheStats by remember { mutableStateOf<CacheStats?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var clearing by remember { mutableStateOf(false) }
    LaunchedEffect(cache, refresh) {
        try { cacheStats = cache.stats() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { cacheStats = null; onError() }
    }
    AppPage(stringResource(Res.string.settings), onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("settings_list"), contentPadding = PaddingValues(vertical = 12.dp)) {
            item { Text(stringResource(Res.string.appearance), Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.theme)) }, supportingContent = { Text(stringResource(Res.string.dark_theme)) }, trailingContent = {
                Switch(settings.darkTheme, { scope.launch { if (repository.setTheme(it).isFailure) onError() } }, Modifier.testTag("dark_theme_switch"))
            }) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.accent_color)) }, trailingContent = { Box(Modifier.size(28.dp).background(Color(settings.accentArgb.toInt()), CircleShape)) }, modifier = Modifier.clickable { accentDialog = true }.testTag("accent_color")) }
            item { HorizontalDivider(Modifier.padding(vertical = 12.dp)) }
            item { Text(stringResource(Res.string.images), Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.data_saver)) }, supportingContent = { Text(stringResource(Res.string.data_saver_explain)) }, trailingContent = {
                Switch(settings.dataSaver, { scope.launch { if (repository.setDataSaver(it).isFailure) onError() } }, Modifier.testTag("data_saver_switch"))
            }) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.cache_images)) }, supportingContent = {
                val stats = cacheStats
                Text(if (stats == null) stringResource(Res.string.cache_unknown) else stringResource(Res.string.cache_usage, stats.memoryBytes / 1024, stats.diskBytes / 1024))
            }) }
            item { TextButton(onClick = { clearing = true; scope.launch { try { if (cache.clear().isFailure) onError(); refresh++ } finally { clearing = false } } }, enabled = !clearing, modifier = Modifier.padding(horizontal = 8.dp)) { Text(stringResource(Res.string.clear_image_cache)) } }
            item { HorizontalDivider(Modifier.padding(vertical = 12.dp)) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.about_this_app)) }, modifier = Modifier.clickable(onClick = onAbout).testTag("open_about")) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.contributors_title)) }, modifier = Modifier.clickable(onClick = onContributors).testTag("open_contributors")) }
            item { ListItem(headlineContent = { Text(stringResource(Res.string.library_licenses)) }, modifier = Modifier.clickable(onClick = onLicenses).testTag("open_licenses")) }
        }
    }
    if (accentDialog) {
        var selected by remember { mutableLongStateOf(settings.accentArgb) }
        val accents = remember(settings.accentArgb) { (listOf(settings.accentArgb) + listOf(0xFF673AB7, 0xFF006C4C, 0xFF00629E, 0xFF904A00, 0xFFAB164B, 0xFF526600)).distinct() }
        AlertDialog(onDismissRequest = { accentDialog = false }, title = { Text(stringResource(Res.string.accent_color)) }, text = {
            LazyColumn {
                items(accents.size) { index ->
                    val argb = accents[index]
                    val hex = argb.toString(16).uppercase()
                    Row(Modifier.fillMaxWidth().clickable { selected = argb }.testTag("accent_$hex").padding(4.dp)) {
                        RadioButton(selected == argb, { selected = argb })
                        Box(Modifier.padding(12.dp).size(24.dp).background(Color(argb.toInt()), CircleShape))
                        Text("#$hex", Modifier.padding(top = 14.dp))
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { scope.launch { if (repository.setAccent(selected).isSuccess) accentDialog = false else onError() } }) { Text(stringResource(Res.string.apply)) } },
            dismissButton = { TextButton(onClick = { accentDialog = false }) { Text(stringResource(Res.string.cancel)) } })
    }
}
