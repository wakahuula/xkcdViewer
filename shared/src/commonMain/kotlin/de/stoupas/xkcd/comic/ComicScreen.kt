package de.stoupas.xkcd.comic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.stoupas.xkcd.app.APP_NAME
import de.stoupas.xkcd.app.AppIcons
import de.stoupas.xkcd.resources.*
import de.stoupas.xkcd.settings.UserSettings
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(
    viewModel: ComicViewModel,
    settings: UserSettings,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
    onShare: (Comic) -> Unit,
    onSave: (Comic, String) -> Unit,
    onExplain: (Comic) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val comic = state.comic
    val available = comic != null && !state.loading && state.failure == null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(APP_NAME, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.testTag("navigate_back")) {
                        Icon(AppIcons.Previous, stringResource(Res.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onFavorites, modifier = Modifier.testTag("open_favorites")) {
                        Icon(AppIcons.Favorites, stringResource(Res.string.my_favorites))
                    }
                    IconButton(onClick = { search = true }, modifier = Modifier.testTag("open_comic_search")) {
                        Icon(AppIcons.Search, stringResource(Res.string.find_comic))
                    }
                    Box {
                        IconButton(onClick = { menu = true }, modifier = Modifier.testTag("comic_menu")) {
                            Icon(AppIcons.More, stringResource(Res.string.more_actions))
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            @Composable fun item(label: String, enabled: Boolean = true, tag: String = label, action: () -> Unit) {
                                DropdownMenuItem(text = { Text(label) }, enabled = enabled, modifier = Modifier.testTag(tag), onClick = { menu = false; action() })
                            }
                            item(stringResource(Res.string.latest_comic)) { viewModel.loadLatest() }
                            item(stringResource(Res.string.settings)) { onSettings() }
                            HorizontalDivider()
                            item(stringResource(Res.string.explain_current), available) { comic?.let(onExplain) }
                            item(stringResource(Res.string.share), available) { comic?.let(onShare) }
                            item(stringResource(Res.string.download), available && state.resolvedImageUrl != null, "save_comic") {
                                val image = state.resolvedImageUrl
                                if (comic != null && image != null) onSave(comic, image)
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.loadAdjacent(-1) }, enabled = available && comic.num > 1) {
                        Icon(AppIcons.Previous, stringResource(Res.string.previous_comic))
                    }
                    TextButton(onClick = { viewModel.loadRandom() }, enabled = !state.loading) { Text(stringResource(Res.string.random)) }
                    IconButton(onClick = { viewModel.loadAdjacent(1) }, enabled = available) {
                        Icon(AppIcons.Next, stringResource(Res.string.next_comic))
                    }
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
        val compact = maxHeight < 280.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (comic != null) {
                Row(Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = if (compact) 0.dp else 4.dp, bottom = if (compact) 0.dp else 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        if (!compact) Text("#${comic.num} · ${comic.year}-${comic.month.padStart(2, '0')}-${comic.day.padStart(2, '0')}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(comic.title, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, maxLines = if (compact) 1 else 2, overflow = TextOverflow.Ellipsis)
                    }
                    val favorite = comic.num in settings.favorites
                    IconToggleButton(checked = favorite, onCheckedChange = { viewModel.toggleFavorite() }, enabled = available, modifier = Modifier.testTag("toggle_favorite")) {
                        Icon(if (favorite) AppIcons.HeartFilled else AppIcons.Heart, stringResource(if (favorite) Res.string.remove_favorite else Res.string.add_favorite))
                    }
                }
            }
            if (state.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().testTag("comic_loading"))
            if (state.failure != null) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val message = when (state.failure) {
                        is ComicFailure.InvalidNumber -> Res.string.invalid_comic_number
                        is ComicFailure.NotFound -> Res.string.comic_not_found
                        else -> Res.string.network_error
                    }
                    Text(stringResource(message), modifier = Modifier.testTag("comic_failure"))
                    Button(onClick = viewModel::retry, modifier = Modifier.testTag("comic_retry")) { Text(stringResource(Res.string.retry)) }
                }
            }
            if (state.settingsWriteFailed) Text(stringResource(Res.string.settings_write_failed), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            if (comic != null) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).widthIn(max = 840.dp).fillMaxWidth()) {
                    ComicViewer(comic, settings.dataSaver, viewModel::resolvedImage)
                }
            } else if (state.loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        }
    }
    if (search) ComicSearchDialog(onDismiss = { search = false }, onSelect = viewModel::select)
}
