package de.stoupas.xkcd.comic

interface ComicApi {
    suspend fun latest(): Result<Comic>
    suspend fun byId(id: Int): Result<Comic>
}

sealed class ComicFailure(message: String) : Exception(message) {
    class InvalidNumber : ComicFailure("Invalid comic number")
    class NotFound : ComicFailure("Comic not found")
    class Network : ComicFailure("Could not load comic")
    class InvalidResponse : ComicFailure("Invalid comic response")
}
