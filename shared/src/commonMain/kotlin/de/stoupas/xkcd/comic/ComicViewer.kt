package de.stoupas.xkcd.comic

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

val LocalComicImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

@Composable
fun ComicViewer(
    comic: Comic,
    dataSaver: Boolean,
    onResolvedImage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    key(comic.num, dataSaver) {
        val loader = LocalComicImageLoader.current ?: SingletonImageLoader.get(LocalPlatformContext.current)
        val candidates = remember(comic.img, dataSaver) { imageCandidates(comic, dataSaver) }
        var candidate by remember { mutableIntStateOf(0) }
        var attempt by remember { mutableIntStateOf(0) }
        var failed by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(true) }
        var altVisible by remember { mutableStateOf(false) }
        val onResolved by rememberUpdatedState(onResolvedImage)
        val density = LocalDensity.current.density
        Column(modifier.fillMaxSize()) {
            key(attempt) {
                val zoom = rememberCoilZoomState()
                var active by remember { mutableStateOf(true) }
                DisposableEffect(Unit) { onDispose { active = false } }
                val url = candidates[candidate]
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CoilZoomAsyncImage(
                        model = url,
                        imageLoader = loader,
                        contentDescription = comic.title,
                        zoomState = zoom,
                        modifier = Modifier.fillMaxSize().testTag("comic_image").semantics {
                            val base = zoom.zoomable.minScale.coerceAtLeast(0.001f)
                            stateDescription = "${(zoom.zoomable.transform.scaleX / base * 100).toInt()}%"
                        }.pointerInput(comic.num, density) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                val baseAtStart = zoom.zoomable.transform.scaleX <= zoom.zoomable.minScale + 0.01f
                                var maxPointers = 1
                                var endY = down.position.y
                                do {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    maxPointers = maxOf(maxPointers, event.changes.count { it.pressed })
                                    event.changes.firstOrNull { it.id == down.id }?.let { endY = it.position.y }
                                    val pressed = event.changes.any { it.pressed }
                                } while (pressed)
                                val baseNow = zoom.zoomable.transform.scaleX <= zoom.zoomable.minScale + 0.01f
                                if (shouldRevealAltText(maxPointers, baseAtStart && baseNow, (down.position.y - endY) / density)) altVisible = true
                            }
                        },
                        onLoading = { if (active) loading = true },
                        onSuccess = {
                            if (active && url == candidates[candidate]) {
                                loading = false
                                failed = false
                                onResolved(url)
                            }
                        },
                        onError = {
                            if (active && url == candidates[candidate]) {
                                if (candidate + 1 < candidates.size) candidate++ else { failed = true; loading = false }
                            }
                        },
                    )
                    if (loading && !failed) CircularProgressIndicator()
                    if (failed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(Res.string.image_load_failed))
                            Button(onClick = { candidate = 0; failed = false; loading = true; attempt++ }, modifier = Modifier.testTag("image_retry")) {
                                Text(stringResource(Res.string.retry))
                            }
                        }
                    }
                }
            }
            TextButton(onClick = { altVisible = true }, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("alt_text_button")) {
                Text(stringResource(Res.string.show_alt_text))
            }
        }
        if (altVisible) {
            AlertDialog(
                onDismissRequest = { altVisible = false },
                title = { Text(comic.title) },
                text = { Text(comic.alt, modifier = Modifier.verticalScroll(rememberScrollState())) },
                confirmButton = { TextButton(onClick = { altVisible = false }) { Text(stringResource(Res.string.hide_alt_text)) } },
            )
        }
    }
}
