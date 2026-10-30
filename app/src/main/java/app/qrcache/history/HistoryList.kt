package app.qrcache.history

import app.qrcache.data.ScanType
import app.qrcache.data.db.ScanEntity
import app.qrcache.scan.domainOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Date groups in the History list, newest first. */
enum class HistorySection { TODAY, YESTERDAY, THIS_WEEK, EARLIER }

/** Filter chips under the search bar. */
enum class HistoryFilter { ALL, FAVORITES, LINKS, WIFI, TEXT, OTHER }

fun sectionFor(timestamp: Long, today: LocalDate, zone: ZoneId): HistorySection {
    val date = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
    return when {
        !date.isBefore(today) -> HistorySection.TODAY
        date == today.minusDays(1) -> HistorySection.YESTERDAY
        date.isAfter(today.minusDays(7)) -> HistorySection.THIS_WEEK
        else -> HistorySection.EARLIER
    }
}

/** Scans that match the search text and the selected chip, in their existing order. */
fun List<ScanEntity>.filterHistory(query: String, filter: HistoryFilter): List<ScanEntity> {
    val q = query.trim()
    return filter { scan ->
        val matchesChip = when (filter) {
            HistoryFilter.ALL -> true
            HistoryFilter.FAVORITES -> scan.favorite
            HistoryFilter.LINKS -> scan.type == ScanType.LINK
            HistoryFilter.WIFI -> scan.type == ScanType.WIFI
            HistoryFilter.TEXT -> scan.type == ScanType.TEXT
            HistoryFilter.OTHER -> scan.type !in setOf(ScanType.LINK, ScanType.WIFI, ScanType.TEXT)
        }
        matchesChip && (
            q.isEmpty() ||
                scan.title.contains(q, ignoreCase = true) ||
                scan.raw.contains(q, ignoreCase = true)
            )
    }
}

/** Groups already-sorted scans into date sections, dropping empty ones. */
fun List<ScanEntity>.groupBySection(today: LocalDate, zone: ZoneId): List<Pair<HistorySection, List<ScanEntity>>> =
    groupBy { sectionFor(it.lastScannedAt, today, zone) }
        .toSortedMap()
        .map { (section, scans) -> section to scans }

/** Short preview under a row's title: the domain for links, otherwise the first line of the text. */
fun ScanEntity.preview(): String = when (type) {
    ScanType.LINK -> domainOf(raw.trim())
    else -> raw.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
}

/** Time for today and yesterday ("8:12 AM"), weekday for this week ("Tue"), otherwise a short date. */
fun timeLabel(timestamp: Long, today: LocalDate, zone: ZoneId, locale: Locale = Locale.getDefault()): String {
    val time = Instant.ofEpochMilli(timestamp).atZone(zone)
    return when (sectionFor(timestamp, today, zone)) {
        HistorySection.TODAY, HistorySection.YESTERDAY ->
            time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        HistorySection.THIS_WEEK -> time.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        HistorySection.EARLIER -> time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))
    }
}
