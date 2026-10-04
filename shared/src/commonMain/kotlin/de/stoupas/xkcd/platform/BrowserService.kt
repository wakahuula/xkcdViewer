package de.stoupas.xkcd.platform

interface BrowserService { suspend fun open(url: String): Result<Unit> }
