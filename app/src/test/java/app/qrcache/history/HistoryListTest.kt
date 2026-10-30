package app.qrcache.history

import app.qrcache.data.ScanType
import app.qrcache.data.db.ScanEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class HistoryListTest {
    private val zone = ZoneId.of("Europe/Paris")
    private val today = LocalDate.of(2026, 10, 30) // a Friday

    private fun at(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0) =
        LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant().toEpochMilli()

    private fun scan(
        id: Long,
        raw: String,
        type: ScanType,
        title: String = raw,
        favorite: Boolean = false,
        at: Long = at(2026, 10, 30),
    ) = ScanEntity(
        id = id, raw = raw, type = type, title = title,
        firstScannedAt = at, lastScannedAt = at, favorite = favorite,
    )

    @Test fun sections() {
        assertEquals(HistorySection.TODAY, sectionFor(at(2026, 10, 30, 0, 1), today, zone))
        assertEquals(HistorySection.YESTERDAY, sectionFor(at(2026, 10, 29, 23, 59), today, zone))
        assertEquals(HistorySection.THIS_WEEK, sectionFor(at(2026, 10, 24), today, zone))
        assertEquals(HistorySection.EARLIER, sectionFor(at(2026, 10, 23), today, zone))
        // Clock skew: a timestamp slightly in the future still counts as today.
        assertEquals(HistorySection.TODAY, sectionFor(at(2026, 10, 31, 0, 5), today, zone))
    }

    @Test fun groupingKeepsOrderAndDropsEmptySections() {
        val scans = listOf(
            scan(1, "a", ScanType.TEXT, at = at(2026, 10, 30, 9)),
            scan(2, "b", ScanType.TEXT, at = at(2026, 10, 30, 8)),
            scan(3, "c", ScanType.TEXT, at = at(2026, 10, 1)),
        )
        val groups = scans.groupBySection(today, zone)
        assertEquals(listOf(HistorySection.TODAY, HistorySection.EARLIER), groups.map { it.first })
        assertEquals(listOf(1L, 2L), groups[0].second.map { it.id })
    }

    @Test fun searchMatchesTitleAndContentIgnoringCase() {
        val scans = listOf(
            scan(1, "https://menu.hotel.com/breakfast", ScanType.LINK, title = "Breakfast menu"),
            scan(2, "WIFI:S:Lobby;P:x;;", ScanType.WIFI, title = "Lobby"),
        )
        assertEquals(listOf(1L), scans.filterHistory("BREAKFAST", HistoryFilter.ALL).map { it.id })
        assertEquals(listOf(1L), scans.filterHistory("hotel.com", HistoryFilter.ALL).map { it.id })
        assertEquals(listOf(1L, 2L), scans.filterHistory("  ", HistoryFilter.ALL).map { it.id })
        assertEquals(emptyList<Long>(), scans.filterHistory("spa", HistoryFilter.ALL).map { it.id })
    }

    @Test fun chips() {
        val scans = listOf(
            scan(1, "https://a.com", ScanType.LINK, favorite = true),
            scan(2, "WIFI:S:x;;", ScanType.WIFI),
            scan(3, "hello", ScanType.TEXT),
            scan(4, "tel:123", ScanType.PHONE),
            scan(5, "geo:1,2", ScanType.GEO, favorite = true),
        )
        fun ids(f: HistoryFilter) = scans.filterHistory("", f).map { it.id }
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), ids(HistoryFilter.ALL))
        assertEquals(listOf(1L, 5L), ids(HistoryFilter.FAVORITES))
        assertEquals(listOf(1L), ids(HistoryFilter.LINKS))
        assertEquals(listOf(2L), ids(HistoryFilter.WIFI))
        assertEquals(listOf(3L), ids(HistoryFilter.TEXT))
        assertEquals(listOf(4L, 5L), ids(HistoryFilter.OTHER))
    }

    @Test fun preview() {
        assertEquals("menu.hotel.com", scan(1, "https://www.menu.hotel.com/x", ScanType.LINK).preview())
        assertEquals("Line one", scan(2, "\nLine one\nLine two", ScanType.TEXT).preview())
    }

    @Test fun timeLabels() {
        val us = Locale.US
        assertEquals("8:12 AM", timeLabel(at(2026, 10, 30, 8, 12), today, zone, us).replace(' ', ' '))
        assertEquals("Sat", timeLabel(at(2026, 10, 24), today, zone, us))
        assertEquals("10/1/26", timeLabel(at(2026, 10, 1), today, zone, us))
    }
}
