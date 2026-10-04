package de.stoupas.xkcd.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidLicenseSource(context: Context, private val resourceId: Int) : LicenseSource {
    private val resources = context.applicationContext.resources
    override suspend fun readJson(): String = withContext(Dispatchers.IO) {
        resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }
}
