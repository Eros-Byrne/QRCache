package app.qrcache.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

/** Looks up a web page's title. Returns null when there's no usable title or anything goes wrong. */
fun interface PageTitleSource {
    suspend fun fetch(url: String): String?
}

/**
 * Downloads the start of a page (at most 64 KB, 5 s timeouts) and reads its title.
 * HTTPS only. Every hop, redirects included, must resolve to a public address, so a QR code
 * can't make the phone talk to the local network.
 */
class TitleFetcher(private val userAgent: String) : PageTitleSource {

    override suspend fun fetch(url: String): String? = withContext(Dispatchers.IO) {
        runCatching { fetchBlocking(url) }.getOrNull()
    }

    private fun fetchBlocking(start: String): String? {
        var url = start
        repeat(MAX_HOPS) {
            if (!isSafeToFetch(url) || !resolvesToPublicAddress(URI(url).host)) return null
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false // checked hop by hop above
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "text/html")
            }
            try {
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location") ?: return null
                    url = URL(URL(url), location).toString()
                    return@repeat
                }
                if (code !in 200..299) return null
                val type = conn.contentType.orEmpty()
                if (!type.contains("html", ignoreCase = true)) return null
                val charset = Regex("charset=\"?([^;\"]+)", RegexOption.IGNORE_CASE).find(type)?.groupValues?.get(1)
                    ?.let { runCatching { Charset.forName(it.trim()) }.getOrNull() }
                    ?: Charsets.UTF_8
                val bytes = conn.inputStream.use { it.readAtMost(MAX_BYTES) }
                return extractPageTitle(String(bytes, charset))
            } finally {
                conn.disconnect()
            }
        }
        return null
    }

    private fun resolvesToPublicAddress(host: String): Boolean {
        val addresses = InetAddress.getAllByName(host)
        return addresses.isNotEmpty() && addresses.none { a ->
            a.isLoopbackAddress || a.isSiteLocalAddress || a.isLinkLocalAddress || a.isAnyLocalAddress ||
                a.isMulticastAddress || isPrivateIpLiteral(a.hostAddress.orEmpty().substringBefore('%'))
        }
    }

    private fun InputStream.readAtMost(limit: Int): ByteArray {
        val buffer = ByteArray(limit)
        var total = 0
        while (total < limit) {
            val n = read(buffer, total, limit - total)
            if (n < 0) break
            total += n
        }
        return buffer.copyOf(total)
    }

    private companion object {
        const val MAX_HOPS = 4
        const val TIMEOUT_MS = 5_000
        const val MAX_BYTES = 64 * 1024
    }
}
