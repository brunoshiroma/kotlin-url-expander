package com.brunoshiroma.url_expander

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GoUrlExpanderClientTest {

    @Test
    fun extractUrlFindsUrlInsideText() {
        assertEquals("https://a.co/x?y=1", extractUrl("look https://a.co/x?y=1 now"))
        assertNull(extractUrl("no link here"))
    }

    @Test
    fun noDataReturnsNull() = runTest {
        val client = GoUrlExpanderClient(HttpClient(MockEngine { respond("x") }))
        assertNull(client.getUrlExpanded("NO DATA"))
    }

    @Test
    fun returnsBodyOnSuccessAndEncodesUrl() = runTest {
        var requested = ""
        val engine = MockEngine { request ->
            requested = request.url.toString()
            respond("https://example.com/long")
        }
        val result = GoUrlExpanderClient(HttpClient(engine), "https://h").getUrlExpanded("see https://t.co/a?b=c")
        assertEquals("https://example.com/long", result)
        assertEquals("https://h/expand?url=https%3A%2F%2Ft.co%2Fa%3Fb%3Dc", requested)
    }

    @Test
    fun nonSuccessReturnsNull() = runTest {
        val engine = MockEngine { respond("", HttpStatusCode.InternalServerError) }
        assertNull(GoUrlExpanderClient(HttpClient(engine)).getUrlExpanded("https://t.co/a"))
    }
}
