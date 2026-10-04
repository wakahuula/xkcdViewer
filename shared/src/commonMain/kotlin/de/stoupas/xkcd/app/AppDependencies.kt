package de.stoupas.xkcd.app

import de.stoupas.xkcd.comic.ComicRepository
import de.stoupas.xkcd.settings.SettingsRepository
import de.stoupas.xkcd.platform.*

data class AppDependencies(
    val comics: ComicRepository,
    val settings: SettingsRepository,
    val share: ShareService,
    val save: ImageSaveService,
    val browser: BrowserService,
    val cache: ImageCacheService,
    val licenses: LicenseSource,
)
