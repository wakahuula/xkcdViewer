package de.stoupas.xkcd.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.stoupas.xkcd.app.*
import de.stoupas.xkcd.resources.*
import de.stoupas.xkcd.settings.SettingsRepository
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoritesScreen(vm: FavoritesViewModel, settings: SettingsRepository, onOpen: (Int) -> Unit, onBack: () -> Unit, onError: () -> Unit) {
    val items by vm.items.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var clear by remember { mutableStateOf(false) }
    AppPage(stringResource(Res.string.my_favorites), onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("favorites_list"), contentPadding = PaddingValues(16.dp)) {
            if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (items.isEmpty()) item { Text(stringResource(Res.string.nothing_here), Modifier.padding(16.dp)) }
            items(items, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.comic?.title ?: "#${item.id}") },
                    supportingContent = {
                        if (item.failure != null) {
                            TextButton(onClick = { vm.retry(item.id) }) { Text(stringResource(Res.string.retry)) }
                        } else Text("#${item.id}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingContent = {
                        IconButton(onClick = { scope.launch { if (settings.removeFavorite(item.id).isFailure) onError() } }) {
                            Icon(AppIcons.Heart, stringResource(Res.string.remove_favorite), tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { onOpen(item.id) }.testTag("favorite_${item.id}"),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (items.isNotEmpty()) item { TextButton(onClick = { clear = true }) { Text(stringResource(Res.string.clear_favorites)) } }
        }
    }
    if (clear) AlertDialog(onDismissRequest = { clear = false }, title = { Text(stringResource(Res.string.clear_favorites)) },
        text = { Text(stringResource(Res.string.clear_favorites_confirmation)) },
        confirmButton = { TextButton(onClick = { clear = false; scope.launch { if (settings.clearFavorites().isFailure) onError() } }) { Text(stringResource(Res.string.yes)) } },
        dismissButton = { TextButton(onClick = { clear = false }) { Text(stringResource(Res.string.cancel)) } })
}
