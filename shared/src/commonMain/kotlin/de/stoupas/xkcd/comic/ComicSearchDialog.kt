package de.stoupas.xkcd.comic

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ComicSearchDialog(onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.find_comic)) },
        text = {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; invalid = false },
                label = { Text(stringResource(Res.string.comic_number)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = invalid,
                supportingText = { if (invalid) Text(stringResource(Res.string.invalid_comic_number)) },
                modifier = Modifier.testTag("comic_number_input"),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val id = parseComicId(query)
                if (id == null) invalid = true else { onSelect(id); onDismiss() }
            }, modifier = Modifier.testTag("comic_number_confirm")) { Text(stringResource(Res.string.open_comic)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
