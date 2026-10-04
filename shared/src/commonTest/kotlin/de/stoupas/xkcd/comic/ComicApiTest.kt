package de.stoupas.xkcd.comic

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ComicApiTest {
    private val valid = """{"num":12,"title":"Comic","safe_title":"Safe comic","alt":"Alt ❤","img":"https://imgs.xkcd.com/comics/test.png","year":"2006","month":"1","day":"1","unknown":123}"""

    @Test
    fun endpointsAndUnknownFieldsAreHandled() = runTest {
        val urls = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            urls.add(request.url.toString())
            respond(valid, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val api = KtorComicApi(client)
        try {
            assertEquals("Alt ❤", api.latest().getOrThrow().alt)
            assertEquals("Safe comic", api.byId(12).getOrThrow().safeTitle)
            assertEquals(listOf("https://www.xkcd.com/info.0.json", "https://www.xkcd.com/12/info.0.json"), urls)
        } finally { api.close(); client.close() }
    }

    @Test
    fun mapsHttpAndInvalidResponsesWithoutInventingComics() = runTest {
        for ((status, body, expected) in listOf(
            Triple(HttpStatusCode.NotFound, "missing", ComicFailure.NotFound::class),
            Triple(HttpStatusCode.InternalServerError, "server", ComicFailure.Network::class),
            Triple(HttpStatusCode.OK, """{"num":"broken"}""", ComicFailure.InvalidResponse::class),
            Triple(HttpStatusCode.OK, valid.replace("\"num\":12", "\"num\":0"), ComicFailure.InvalidResponse::class),
        )) {
            val client = HttpClient(MockEngine { respond(body, status) })
            val api = KtorComicApi(client)
            try { assertEquals(expected, api.byId(12).exceptionOrNull()!!::class) }
            finally { api.close(); client.close() }
        }
    }

    @Test
    fun invalidInputDoesNotStartNetworkRequest() = runTest {
        val client = HttpClient(MockEngine { error("Must not request invalid IDs") })
        val api = KtorComicApi(client)
        try {
            assertIs<ComicFailure.InvalidNumber>(api.byId(0).exceptionOrNull())
            assertIs<ComicFailure.NotFound>(api.byId(404).exceptionOrNull())
        } finally { api.close(); client.close() }
    }

    @Test
    fun cancellationIsNotTurnedIntoNetworkFailure() = runTest {
        val client = HttpClient(MockEngine { throw CancellationException("Cancelled") })
        val api = KtorComicApi(client)
        try { assertFailsWith<CancellationException> { api.latest() } }
        finally { api.close(); client.close() }
    }
}
