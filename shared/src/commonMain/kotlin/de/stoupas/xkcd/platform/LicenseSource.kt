package de.stoupas.xkcd.platform

interface LicenseSource { suspend fun readJson(): String }
