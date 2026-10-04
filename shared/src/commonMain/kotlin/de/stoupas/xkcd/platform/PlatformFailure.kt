package de.stoupas.xkcd.platform

sealed class PlatformFailure(message: String) : Exception(message) {
    data object UserCancelled : PlatformFailure("Operation cancelled")
    class Unavailable : PlatformFailure("Operation unavailable")
    class InvalidUrl : PlatformFailure("Unsupported URL")
    class WriteFailed : PlatformFailure("Could not save image")
}
