package app.qrcache.data

import java.net.URI

private const val MAX_TITLE = 100

private val ogTitle = Regex(
    """<meta\s[^>]*?(?:property|name)\s*=\s*["']og:title["'][^>]*>""",
    setOf(RegexOption.IGNORE_CASE),
)
private val contentAttr = Regex("""content\s*=\s*(?:"([^"]*)"|'([^']*)')""", RegexOption.IGNORE_CASE)
private val titleTag = Regex("""<title[^>]*>(.*?)</title\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

/**
 * Finds a page's title in its HTML: `og:title` first (usually the cleanest), then `<title>`.
 * Returns null if neither is there or both are blank.
 */
fun extractPageTitle(html: String): String? {
    val og = ogTitle.find(html)?.value?.let { tag ->
        contentAttr.find(tag)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }
    }
    val title = titleTag.find(html)?.groupValues?.get(1)
    return listOfNotNull(og, title)
        .map { cleanTitle(decodeEntities(it)) }
        .firstOrNull { it.isNotEmpty() }
}

private fun cleanTitle(s: String): String {
    val collapsed = s.replace(Regex("\\s+"), " ").trim()
    return if (collapsed.length > MAX_TITLE) collapsed.take(MAX_TITLE - 1).trimEnd() + "…" else collapsed
}

private val entity = Regex("&(#[0-9]+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);")
private val named = mapOf(
    "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " ",
    "ndash" to "–", "mdash" to "—", "hellip" to "…", "rsquo" to "’", "lsquo" to "‘",
    "rdquo" to "”", "ldquo" to "“", "middot" to "·", "bull" to "•", "copy" to "©", "reg" to "®",
)

internal fun decodeEntities(s: String): String = entity.replace(s) { m ->
    val body = m.groupValues[1]
    val codePoint = when {
        body.startsWith("#x") || body.startsWith("#X") -> body.drop(2).toIntOrNull(16)
        body.startsWith("#") -> body.drop(1).toIntOrNull()
        else -> null
    }
    when {
        codePoint != null && Character.isValidCodePoint(codePoint) && codePoint != 0 -> String(Character.toChars(codePoint))
        codePoint != null -> m.value
        else -> named[body.lowercase()] ?: m.value
    }
}

/**
 * Whether QRCache may load [url] to read its title: HTTPS only, and never local network addresses,
 * so a QR code can't make the phone poke a router or another device on the Wi-Fi.
 */
fun isSafeToFetch(url: String): Boolean {
    val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    val host = uri.host?.lowercase()?.trimEnd('.') ?: return false
    if (host == "localhost" || host.endsWith(".localhost") || host.endsWith(".local") ||
        host.endsWith(".lan") || host.endsWith(".internal") || host.endsWith(".home.arpa")
    ) {
        return false
    }
    return !isPrivateIpLiteral(host)
}

/** Loopback, private, link-local, CGNAT and unique-local addresses written as IP literals. */
internal fun isPrivateIpLiteral(host: String): Boolean {
    val h = host.removePrefix("[").removeSuffix("]")
    val v4 = h.split('.').takeIf { it.size == 4 }?.map { it.toIntOrNull() ?: return false }
    if (v4 != null) {
        if (v4.any { it !in 0..255 }) return false
        val (a, b) = v4
        return a == 0 || a == 10 || a == 127 ||
            (a == 169 && b == 254) ||
            (a == 172 && b in 16..31) ||
            (a == 192 && b == 168) ||
            (a == 100 && b in 64..127)
    }
    if (':' in h) {
        val lower = h.lowercase()
        return lower == "::" || lower == "::1" ||
            lower.startsWith("fc") || lower.startsWith("fd") ||
            lower.startsWith("fe8") || lower.startsWith("fe9") || lower.startsWith("fea") || lower.startsWith("feb") ||
            lower.startsWith("::ffff:")
    }
    return false
}
