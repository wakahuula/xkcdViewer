package de.stoupas.xkcd.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidBrowserService(private val context: Context, private val launch: (Intent) -> Unit = { context.startActivity(it) }) : BrowserService {
    override suspend fun open(url: String): Result<Unit> = withContext(Dispatchers.Main) {
        val uri = Uri.parse(url)
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank() || uri.isOpaque) {
            return@withContext Result.failure(PlatformFailure.InvalidUrl())
        }
        try {
            val customTab = CustomTabsIntent.Builder().build().intent.apply {
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try { launch(customTab) }
            catch (_: android.content.ActivityNotFoundException) {
                launch(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            Result.success(Unit)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Result.failure(PlatformFailure.Unavailable()) }
    }
}
