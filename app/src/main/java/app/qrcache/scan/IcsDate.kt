package app.qrcache.scan

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
private val dateOnly = DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * Converts an iCalendar date (`20261030T070000Z`, `20261030T070000` or `20261030`)
 * to epoch millis. Times without `Z` are read in [zone]. Returns null if unreadable.
 */
fun icsDateToEpochMillis(value: String, zone: ZoneId = ZoneId.systemDefault()): Long? =
    runCatching {
        val v = value.trim()
        when {
            v.endsWith("Z", ignoreCase = true) ->
                LocalDateTime.parse(v.dropLast(1), dateTime).toInstant(ZoneOffset.UTC).toEpochMilli()
            'T' in v -> LocalDateTime.parse(v, dateTime).atZone(zone).toInstant().toEpochMilli()
            else -> LocalDate.parse(v, dateOnly).atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }.getOrNull()
