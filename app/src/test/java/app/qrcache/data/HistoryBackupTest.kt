package app.qrcache.data

import app.qrcache.data.db.ScanEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Runs on Robolectric because org.json is part of the Android framework. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoryBackupTest {

    private val menu = ScanEntity(
        id = 7, raw = "https://menu.hotel.com", type = ScanType.LINK, title = "Hotel breakfast menu",
        titleIsCustom = true, firstScannedAt = 1_000, lastScannedAt = 5_000, scanCount = 3, favorite = true,
    )
    private val wifi = ScanEntity(
        id = 8, raw = "WIFI:S:Lobby;P:x;;", type = ScanType.WIFI, title = "Lobby",
        firstScannedAt = 2_000, lastScannedAt = 2_000,
    )

    @Test fun jsonRoundTripKeepsEverythingButIds() {
        val back = HistoryBackup.fromJson(HistoryBackup.toJson(listOf(menu, wifi)))
        assertEquals(listOf(menu.copy(id = 0), wifi.copy(id = 0)), back)
    }

    @Test fun importRecomputesTypeAndDefaultTitles() {
        val json = """
            {"format":"qrcache-history","version":1,"scans":[
              {"raw":"tel:+15550100","title":"stale","titleIsCustom":false,"lastScannedAt":9},
              {"raw":"","title":"empty"},
              "not an object"
            ]}
        """.trimIndent()
        val scans = HistoryBackup.fromJson(json)
        assertEquals(1, scans.size)
        assertEquals(ScanType.PHONE, scans[0].type)
        assertEquals("+15550100", scans[0].title)
        assertEquals(9L, scans[0].firstScannedAt)
        assertEquals(1, scans[0].scanCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOtherJson() {
        HistoryBackup.fromJson("""{"hello":"world"}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonJson() {
        HistoryBackup.fromJson("title,content\nx,y")
    }

    @Test fun csvHasHeaderAndQuotesWhenNeeded() {
        val csv = HistoryBackup.toCsv(listOf(menu.copy(title = "Menu, \"breakfast\"")))
        val lines = csv.trimEnd().lines()
        assertEquals("title,content,type,first_scanned,last_scanned,scan_count,favorite", lines[0])
        assertTrue(lines[1].startsWith("\"Menu, \"\"breakfast\"\"\",https://menu.hotel.com,link,1970-01-01T00:00:01Z,"))
        assertTrue(lines[1].endsWith(",3,true"))
    }

    @Test fun csvDefusesSpreadsheetFormulas() {
        assertEquals("'=HYPERLINK(1)", HistoryBackup.csvField("=HYPERLINK(1)"))
        assertEquals("\"'=A1,B1\"", HistoryBackup.csvField("=A1,B1"))
        assertFalse(HistoryBackup.csvField("plain").contains('"'))
    }
}
