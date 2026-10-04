package de.stoupas.xkcd.platform

import de.stoupas.xkcd.comic.Comic

interface ImageSaveService { suspend fun save(comic: Comic, imageUrl: String): Result<Unit> }
