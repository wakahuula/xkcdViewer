package de.stoupas.xkcd.platform

import de.stoupas.xkcd.comic.Comic

interface ShareService { suspend fun shareComic(comic: Comic): Result<Unit> }
