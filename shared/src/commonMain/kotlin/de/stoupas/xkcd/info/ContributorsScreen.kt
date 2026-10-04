package de.stoupas.xkcd.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.stoupas.xkcd.app.AppPage
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContributorsScreen(onBack: () -> Unit, onProfile: (String) -> Unit) {
    var contributors by remember { mutableStateOf<List<Contributor>?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try { contributors = loadContributors() }
        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
    }
    AppPage(stringResource(Res.string.contributors_title), onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("contributors_list"), contentPadding = PaddingValues(16.dp)) {
            if (contributors == null && !failed) item { CircularProgressIndicator() }
            if (failed) item { Text(stringResource(Res.string.something_wrong)) }
            items(contributors.orEmpty(), key = { it.profile }) { contributor ->
                ListItem(headlineContent = { Text(contributor.name) }, supportingContent = { Text(contributor.profile) }, modifier = Modifier.clickable { onProfile(contributor.profile) })
            }
        }
    }
}
