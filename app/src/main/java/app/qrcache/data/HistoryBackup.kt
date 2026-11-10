package app.qrcache.data

import app.qrcache.data.db.ScanEntity
import app.qrcache.scan.ContentParser
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Export and import formats for scan history.
 * JSON keeps everything and can be imported back. CSV is for spreadsheets.
 */
object HistoryBackup {
    private const val FORMAT = "qrcache-history"
    private const val VERSION = 1

    fun toJson(scans: List<ScanEntity>): String {
        val items = JSONArray()
        scans.forEach { s ->
            items.put(
                JSONObject()
                    .put("raw", s.raw)
                    .put("title", s.title)
                    .put("titleIsCustom", s.titleIsCustom)
                    .put("firstScannedAt", s.firstScannedAt)
                    .put("lastScannedAt", s.lastScannedAt)
                    .put("scanCount", s.scanCount)
                    .put("favorite", s.favorite)
                    .put("barcode", s.barcode),
            )
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("scans", items)
            .toString(2)
    }

    /**
     * Reads a JSON export. Entries without content are skipped. The type is worked out again from the
     * content, so files from older versions keep working. Throws [IllegalArgumentException] if the file
     * isn't a QRCache export.
     */
    fun fromJson(json: String): List<ScanEntity> {
        val root = runCatching { JSONObject(json) }.getOrNull()
        require(root != null && root.optString("format") == FORMAT) { "Not a QRCache history file" }
        val items = root.optJSONArray("scans") ?: return emptyList()
        return (0 until items.length()).mapNotNull { i ->
            val o = items.optJSONObject(i) ?: return@mapNotNull null
            val raw = o.optString("raw").takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val content = ContentParser.parse(raw)
            val last = o.optLong("lastScannedAt", 0L)
            val first = o.optLong("firstScannedAt", last)
            val custom = o.optBoolean("titleIsCustom", false)
            ScanEntity(
                raw = raw,
                type = content.scanType,
                title = if (custom) o.optString("title").ifBlank { content.defaultTitle } else content.defaultTitle,
                titleIsCustom = custom && o.optString("title").isNotBlank(),
                firstScannedAt = first,
                lastScannedAt = last,
                scanCount = o.optInt("scanCount", 1).coerceAtLeast(1),
                favorite = o.optBoolean("favorite", false),
                barcode = o.optBoolean("barcode", false),
            )
        }
    }

    fun toCsv(scans: List<ScanEntity>): String = buildString {
        appendLine("title,content,type,first_scanned,last_scanned,scan_count,favorite,code")
        scans.forEach { s ->
            appendLine(
                listOf(
                    s.title,
                    s.raw,
                    s.type.name.lowercase(),
                    Instant.ofEpochMilli(s.firstScannedAt).toString(),
                    Instant.ofEpochMilli(s.lastScannedAt).toString(),
                    s.scanCount.toString(),
                    s.favorite.toString(),
                    if (s.barcode) "barcode" else "qr",
                ).joinToString(",") { csvField(it) },
            )
        }
    }

    /** Quotes a field when needed (RFC 4180), and defuses spreadsheet formulas. */
    internal fun csvField(value: String): String {
        val safe = if (value.isNotEmpty() && value[0] in "=+-@\t\r") "'$value" else value
        val needsQuotes = safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + safe.replace("\"", "\"\"") + "\"" else safe
    }
}
