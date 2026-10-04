package de.stoupas.xkcd.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.stoupas.xkcd.resources.Res
import de.stoupas.xkcd.resources.foundation_status
import org.jetbrains.compose.resources.stringResource

@Composable
fun XkcdApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(APP_NAME, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(Res.string.foundation_status))
                }
            }
        }
    }
}
