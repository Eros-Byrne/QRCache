package app.qrcache.scan

/** What a QR code contains, parsed from its raw text. */
sealed interface ScanContent {
    val raw: String

    /** A readable name for the scan, used as its default title. */
    val defaultTitle: String

    data class Link(override val raw: String, val url: String) : ScanContent {
        override val defaultTitle: String get() = domainOf(url)
    }

    data class Wifi(
        override val raw: String,
        val ssid: String,
        val password: String?,
        val security: String?,
        val hidden: Boolean,
    ) : ScanContent {
        override val defaultTitle: String get() = ssid.ifBlank { "Wi-Fi network" }
    }

    data class Contact(
        override val raw: String,
        val name: String?,
        val phone: String?,
        val email: String?,
        val organization: String?,
    ) : ScanContent {
        override val defaultTitle: String get() = name ?: organization ?: phone ?: email ?: "Contact"
    }

    data class Email(
        override val raw: String,
        val address: String,
        val subject: String?,
        val body: String?,
    ) : ScanContent {
        override val defaultTitle: String get() = address.ifBlank { "Email" }
    }

    data class Phone(override val raw: String, val number: String) : ScanContent {
        override val defaultTitle: String get() = number
    }

    data class Sms(override val raw: String, val number: String, val message: String?) : ScanContent {
        override val defaultTitle: String get() = number.ifBlank { "Text message" }
    }

    data class Geo(override val raw: String, val latitude: Double, val longitude: Double, val query: String?) :
        ScanContent {
        override val defaultTitle: String get() = query ?: "$latitude, $longitude"
    }

    data class Event(
        override val raw: String,
        val summary: String?,
        val location: String?,
        /** iCalendar date-time strings as written in the code, e.g. 20261030T090000Z. */
        val start: String?,
        val end: String?,
    ) : ScanContent {
        override val defaultTitle: String get() = summary ?: "Event"
    }

    data class Text(override val raw: String) : ScanContent {
        override val defaultTitle: String
            get() = raw.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
                ?.let { if (it.length > TITLE_MAX) it.take(TITLE_MAX - 1).trimEnd() + "…" else it }
                ?: "Text"
    }
}

private const val TITLE_MAX = 60
private const val HEX_DIGITS = "0123456789abcdefABCDEF"

/** Turns the raw text of a QR code into typed [ScanContent]. Pure Kotlin, no Android imports. */
object ContentParser {

    fun parse(raw: String): ScanContent {
        val text = raw.trim()
        return when {
            text.startsWith("WIFI:", ignoreCase = true) -> parseWifi(raw, text)
            text.startsWith("BEGIN:VCARD", ignoreCase = true) -> parseVCard(raw, text)
            text.startsWith("MECARD:", ignoreCase = true) -> parseMeCard(raw, text)
            text.startsWith("BEGIN:VEVENT", ignoreCase = true) ||
                text.startsWith("BEGIN:VCALENDAR", ignoreCase = true) -> parseEvent(raw, text)
            text.startsWith("mailto:", ignoreCase = true) -> parseMailto(raw, text)
            text.startsWith("MATMSG:", ignoreCase = true) -> parseMatmsg(raw, text)
            text.startsWith("tel:", ignoreCase = true) -> ScanContent.Phone(raw, text.substring(4).trim())
            text.startsWith("smsto:", ignoreCase = true) || text.startsWith("sms:", ignoreCase = true) ||
                text.startsWith("mmsto:", ignoreCase = true) -> parseSms(raw, text)
            text.startsWith("geo:", ignoreCase = true) -> parseGeo(raw, text) ?: ScanContent.Text(raw)
            isUrl(text) -> ScanContent.Link(raw, normalizeUrl(text))
            else -> ScanContent.Text(raw)
        }
    }

    // --- Links ---

    private val schemeUrl = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)
    private val bareDomain = Regex("^(www\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}(:\\d+)?(/\\S*)?$", RegexOption.IGNORE_CASE)

    private fun isUrl(text: String) = schemeUrl.matches(text) || bareDomain.matches(text)

    private fun normalizeUrl(text: String): String {
        if (schemeUrl.matches(text)) return text
        return "https://$text"
    }

    // --- Wi-Fi: WIFI:T:WPA;S:name;P:pass;H:false;; ---

    private fun parseWifi(raw: String, text: String): ScanContent {
        val fields = splitFields(text.substring(5))
        return ScanContent.Wifi(
            raw = raw,
            ssid = fields["S"].orEmpty(),
            password = fields["P"]?.takeIf { it.isNotEmpty() },
            security = fields["T"]?.takeIf { it.isNotEmpty() && !it.equals("nopass", ignoreCase = true) },
            hidden = fields["H"].equals("true", ignoreCase = true),
        )
    }

    // --- MECARD:N:Doe,Jane;TEL:123;EMAIL:a@b.c;; ---

    private fun parseMeCard(raw: String, text: String): ScanContent {
        val fields = splitFields(text.substring(7))
        val name = fields["N"]?.let { n ->
            // MECARD names are "Last,First"
            val parts = n.split(',', limit = 2).map { it.trim() }
            if (parts.size == 2) "${parts[1]} ${parts[0]}".trim() else n.trim()
        }?.takeIf { it.isNotEmpty() }
        return ScanContent.Contact(
            raw = raw,
            name = name,
            phone = fields["TEL"]?.takeIf { it.isNotEmpty() },
            email = fields["EMAIL"]?.takeIf { it.isNotEmpty() },
            organization = fields["ORG"]?.takeIf { it.isNotEmpty() },
        )
    }

    // --- MATMSG:TO:a@b.c;SUB:Hi;BODY:Text;; ---

    private fun parseMatmsg(raw: String, text: String): ScanContent {
        val fields = splitFields(text.substring(7))
        return ScanContent.Email(
            raw = raw,
            address = fields["TO"].orEmpty(),
            subject = fields["SUB"]?.takeIf { it.isNotEmpty() },
            body = fields["BODY"]?.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * Splits `K:value;K2:value2;;` into a map. Backslash escapes `\;`, `\:`, `\,` and `\\`.
     * First occurrence of a key wins.
     */
    internal fun splitFields(body: String): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        val current = StringBuilder()
        var i = 0
        fun flush() {
            val field = current.toString()
            current.clear()
            val colon = indexOfUnescaped(field, ':')
            if (colon > 0) {
                val key = field.substring(0, colon).uppercase()
                val value = unescape(field.substring(colon + 1))
                result.putIfAbsent(key, value)
            }
        }
        while (i < body.length) {
            val c = body[i]
            when {
                c == '\\' && i + 1 < body.length -> {
                    current.append(c).append(body[i + 1])
                    i += 2
                    continue
                }
                c == ';' -> flush()
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) flush()
        return result
    }

    private fun indexOfUnescaped(s: String, target: Char): Int {
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\') {
                i += 2
                continue
            }
            if (s[i] == target) return i
            i++
        }
        return -1
    }

    private fun unescape(s: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length) {
                out.append(s[i + 1])
                i += 2
            } else {
                out.append(s[i])
                i++
            }
        }
        return out.toString()
    }

    // --- vCard ---

    private fun parseVCard(raw: String, text: String): ScanContent {
        val props = icsProperties(text)
        val fn = props["FN"]
        val n = props["N"]?.let { n ->
            // N:Last;First;Middle;Prefix;Suffix
            val parts = n.split(';').map { it.trim() }
            listOfNotNull(parts.getOrNull(1), parts.getOrNull(0)).filter { it.isNotEmpty() }.joinToString(" ")
        }
        return ScanContent.Contact(
            raw = raw,
            name = (fn ?: n)?.takeIf { it.isNotEmpty() },
            phone = props["TEL"]?.takeIf { it.isNotEmpty() },
            email = props["EMAIL"]?.takeIf { it.isNotEmpty() },
            organization = props["ORG"]?.substringBefore(';')?.takeIf { it.isNotEmpty() },
        )
    }

    // --- iCalendar event ---

    private fun parseEvent(raw: String, text: String): ScanContent {
        val props = icsProperties(text)
        return ScanContent.Event(
            raw = raw,
            summary = props["SUMMARY"]?.takeIf { it.isNotEmpty() },
            location = props["LOCATION"]?.takeIf { it.isNotEmpty() },
            start = props["DTSTART"]?.takeIf { it.isNotEmpty() },
            end = props["DTEND"]?.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * Reads `NAME;PARAMS:value` lines from vCard/iCalendar text. Unfolds continuation lines,
     * drops parameters (`TEL;TYPE=CELL:` → `TEL`) and keeps the first value of each name.
     */
    internal fun icsProperties(text: String): Map<String, String> {
        val lines = mutableListOf<String>()
        for (line in text.split("\r\n", "\n", "\r")) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && lines.isNotEmpty()) {
                lines[lines.lastIndex] = lines.last() + line.substring(1)
            } else {
                lines += line
            }
        }
        val result = LinkedHashMap<String, String>()
        for (line in lines) {
            val colon = line.indexOf(':')
            if (colon <= 0) continue
            val name = line.substring(0, colon).substringBefore(';').uppercase().trim()
            val value = unescapeText(line.substring(colon + 1).trim())
            result.putIfAbsent(name, value)
        }
        return result
    }

    /** vCard/iCalendar text escapes: backslash-n is a newline, backslash before any other character keeps that character. */
    private fun unescapeText(s: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                val next = s[i + 1]
                out.append(if (next == 'n' || next == 'N') '\n' else next)
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }

    // --- mailto:a@b.c?subject=Hi&body=Text ---

    private fun parseMailto(raw: String, text: String): ScanContent {
        val rest = text.substring(7)
        val address = percentDecode(rest.substringBefore('?'))
        val query = queryParams(rest.substringAfter('?', ""))
        return ScanContent.Email(
            raw = raw,
            address = address,
            subject = query["subject"]?.takeIf { it.isNotEmpty() },
            body = query["body"]?.takeIf { it.isNotEmpty() },
        )
    }

    // --- smsto:+123:Hello / sms:+123?body=Hello ---

    private fun parseSms(raw: String, text: String): ScanContent {
        val rest = text.substringAfter(':')
        val number: String
        val message: String?
        if ('?' in rest) {
            number = rest.substringBefore('?')
            message = queryParams(rest.substringAfter('?'))["body"]
        } else {
            number = rest.substringBefore(':')
            message = rest.substringAfter(':', "").takeIf { it.isNotEmpty() }
        }
        return ScanContent.Sms(raw, percentDecode(number).trim(), message?.takeIf { it.isNotEmpty() })
    }

    // --- geo:12.34,-56.78?q=Hotel ---

    private fun parseGeo(raw: String, text: String): ScanContent? {
        val rest = text.substring(4)
        val coords = rest.substringBefore('?').substringBefore(';').split(',')
        val lat = coords.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return null
        val lng = coords.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return null
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
        val q = queryParams(rest.substringAfter('?', ""))["q"]?.takeIf { it.isNotEmpty() }
        return ScanContent.Geo(raw, lat, lng, q)
    }

    private fun queryParams(query: String): Map<String, String> =
        query.split('&').filter { '=' in it }.associate {
            // In query strings `+` means a space; elsewhere (phone numbers) it is a literal plus.
            fun decode(part: String) = percentDecode(part.replace('+', ' '))
            decode(it.substringBefore('=')).lowercase() to decode(it.substringAfter('='))
        }

    /** Decodes %XX escapes (UTF-8). Leaves malformed escapes as-is. */
    internal fun percentDecode(s: String): String {
        if ('%' !in s) return s
        val bytes = java.io.ByteArrayOutputStream()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '%' && i + 2 < s.length) {
                val hex = s.substring(i + 1, i + 3)
                if (hex.all { it in HEX_DIGITS }) {
                    val b = hex.toInt(16)
                    bytes.write(b)
                    i += 3
                    continue
                }
            }
            // Copy a whole code point so emoji (surrogate pairs) survive.
            val end = i + Character.charCount(s.codePointAt(i))
            bytes.write(s.substring(i, end).toByteArray(Charsets.UTF_8))
            i = end
        }
        return bytes.toString("UTF-8")
    }
}

/** `https://www.menu.hotel.com/breakfast` → `menu.hotel.com`. */
internal fun domainOf(url: String): String {
    val afterScheme = url.substringAfter("://", url)
    val host = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        .substringAfter('@').substringBefore(':')
    return host.removePrefix("www.").removePrefix("WWW.").ifEmpty { url }
}
