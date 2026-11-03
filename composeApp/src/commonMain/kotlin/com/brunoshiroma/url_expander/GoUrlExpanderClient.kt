package com.brunoshiroma.url_expander

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

private val urlRegex = Regex("""(https?://\S+)""")

fun extractUrl(text: String): String? = urlRegex.find(text)?.groupValues?.get(1)

class GoUrlExpanderClient(
    private val client: HttpClient = HttpClient(),
    private val baseUrl: String = "https://go-url-expander.brunoshiroma.com",
) {
    suspend fun getUrlExpanded(text: String): String? {
        if (text == NO_DATA) return null
        val url = extractUrl(text) ?: return null

        val response = client.get("$baseUrl/expand") { parameter("url", url) }
        return if (response.status.isSuccess()) response.body<String>() else null
    }

    companion object {
        const val NO_DATA = "NO DATA"
    }
}
