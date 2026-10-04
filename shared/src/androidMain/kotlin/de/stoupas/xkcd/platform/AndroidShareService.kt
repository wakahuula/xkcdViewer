package de.stoupas.xkcd.platform

import android.content.Context
import android.content.Intent
import de.stoupas.xkcd.comic.Comic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidShareService(private val context: Context, private val launch: (Intent) -> Unit = { context.startActivity(it) }) : ShareService {
    override suspend fun shareComic(comic: Comic): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            require(comic.num > 0 && comic.num != 404)
            val payload = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://www.xkcd.com/${comic.num}/")
            }
            launch(Intent.createChooser(payload, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Result.success(Unit)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Result.failure(PlatformFailure.Unavailable()) }
    }
}
