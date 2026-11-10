package app.qrcache.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.qrcache.data.db.QRCacheDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScanRepositoryTest {
    private lateinit var db: QRCacheDatabase
    private lateinit var repo: ScanRepository
    private var now = 1_000L

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), QRCacheDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = ScanRepository(db.scanDao(), clock = { now })
    }

    @After fun tearDown() = db.close()

    @Test fun newScanIsSavedWithTypeAndDefaultTitle() = runBlocking {
        val id = repo.recordScan("https://www.menu.hotel.com/breakfast")
        val scan = repo.observe(id).first()!!
        assertEquals(ScanType.LINK, scan.type)
        assertEquals("menu.hotel.com", scan.title)
        assertFalse(scan.titleIsCustom)
        assertEquals(1, scan.scanCount)
        assertEquals(1_000L, scan.firstScannedAt)
    }

    @Test fun scansRememberWhetherTheyWereBarcodes() = runBlocking {
        val qr = repo.observe(repo.recordScan("https://menu.example")).first()!!
        val product = repo.observe(repo.recordScan("5012345678900", barcode = true)).first()!!
        assertFalse(qr.barcode)
        assertTrue(product.barcode)
    }

    @Test fun rescanMovesToTopInsteadOfDuplicating() = runBlocking {
        val menu = repo.recordScan("https://menu.example")
        now = 2_000
        repo.recordScan("WIFI:S:Lobby;;")
        now = 3_000
        val again = repo.recordScan("https://menu.example")

        assertEquals(menu, again)
        val all = repo.observeAll().first()
        assertEquals(2, all.size)
        assertEquals(menu, all.first().id)
        assertEquals(2, all.first().scanCount)
        assertEquals(1_000L, all.first().firstScannedAt)
        assertEquals(3_000L, all.first().lastScannedAt)
    }

    @Test fun rescanKeepsCustomTitle() = runBlocking {
        val id = repo.recordScan("https://menu.example")
        repo.rename(id, "  Hotel breakfast menu ")
        repo.recordScan("https://menu.example")
        val scan = repo.observe(id).first()!!
        assertEquals("Hotel breakfast menu", scan.title)
        assertTrue(scan.titleIsCustom)
    }

    @Test fun blankRenameRestoresDefaultTitle() = runBlocking {
        val id = repo.recordScan("https://menu.example")
        repo.rename(id, "Menu")
        repo.rename(id, "   ")
        val scan = repo.observe(id).first()!!
        assertEquals("menu.example", scan.title)
        assertFalse(scan.titleIsCustom)
    }

    @Test fun linkTitleReplacesDomainButNotCustomNames() = runBlocking {
        val fetched = mutableListOf<String>()
        val repo = ScanRepository(db.scanDao(), clock = { now }, titles = { url ->
            fetched += url
            "Grand Hotel – Breakfast Menu"
        })
        val menu = repo.recordScan("https://menu.hotel.com/breakfast")
        repo.refreshLinkTitle(menu)
        assertEquals("Grand Hotel – Breakfast Menu", repo.observe(menu).first()!!.title)
        assertFalse(repo.observe(menu).first()!!.titleIsCustom)

        // Already has a fetched title: no second download.
        repo.refreshLinkTitle(menu)
        assertEquals(1, fetched.size)

        // A name the user chose is never overwritten.
        val spa = repo.recordScan("https://spa.hotel.com")
        repo.rename(spa, "My spa")
        repo.refreshLinkTitle(spa)
        assertEquals("My spa", repo.observe(spa).first()!!.title)

        // Only links are looked up.
        repo.refreshLinkTitle(repo.recordScan("hello"))
        assertEquals(1, fetched.size)
    }

    @Test fun failedTitleLookupKeepsDomain() = runBlocking {
        val repo = ScanRepository(db.scanDao(), clock = { now }, titles = { null })
        val id = repo.recordScan("https://menu.hotel.com")
        repo.refreshLinkTitle(id)
        assertEquals("menu.hotel.com", repo.observe(id).first()!!.title)
    }

    @Test fun importAddsOnlyNewCodes() = runBlocking {
        repo.recordScan("https://menu.example")
        val exported = repo.exportJson()

        // Same file into a fresh history: everything comes back.
        repo.clearAll()
        assertEquals(1, repo.importJson(exported))
        // Importing again adds nothing.
        assertEquals(0, repo.importJson(exported))
        assertEquals(1, repo.observeAll().first().size)
    }

    @Test fun clearAllRemovesEverything() = runBlocking {
        repo.recordScan("a")
        repo.recordScan("b")
        repo.clearAll()
        assertTrue(repo.observeAll().first().isEmpty())
    }

    @Test fun favoriteDeleteAndUndo() = runBlocking {
        val id = repo.recordScan("hello")
        repo.setFavorite(id, true)
        val scan = repo.observe(id).first()!!
        assertTrue(scan.favorite)

        repo.delete(scan)
        assertNull(repo.observe(id).first())

        repo.restore(scan)
        assertEquals(scan, repo.observe(id).first())
    }
}
