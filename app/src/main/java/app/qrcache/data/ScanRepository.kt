package app.qrcache.data

import app.qrcache.data.db.ScanDao
import app.qrcache.data.db.ScanEntity
import app.qrcache.scan.ContentParser
import app.qrcache.scan.ScanContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** The only way the app reads or writes scan history. */
class ScanRepository(
    private val dao: ScanDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val titles: PageTitleSource? = null,
    /** Outlives screens, so a title lookup finishes even if you leave the Scan tab. */
    private val backgroundScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    fun observeAll(): Flow<List<ScanEntity>> = dao.observeAll()

    fun observe(id: Long): Flow<ScanEntity?> = dao.observe(id)

    /** Saves a freshly scanned code and returns its id. */
    suspend fun recordScan(raw: String): Long {
        val content = ContentParser.parse(raw)
        return dao.recordScan(raw, content.scanType, content.defaultTitle, clock())
    }

    /**
     * Replaces a link's automatic name (its domain) with the page's real title.
     * Never touches a name the user chose, and skips links that already have a fetched title.
     */
    suspend fun refreshLinkTitle(id: Long) {
        val source = titles ?: return
        val scan = dao.get(id) ?: return
        if (scan.type != ScanType.LINK || scan.titleIsCustom) return
        val link = scan.content as? ScanContent.Link ?: return
        if (scan.title != link.defaultTitle) return
        val title = source.fetch(link.url) ?: return
        dao.setAutoTitle(id, title)
    }

    fun refreshLinkTitleLater(id: Long) {
        backgroundScope.launch { refreshLinkTitle(id) }
    }

    /** Renames a scan. A blank name goes back to the automatic one. */
    suspend fun rename(id: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            val scan = dao.get(id) ?: return
            dao.setTitle(id, scan.content.defaultTitle, isCustom = false)
        } else {
            dao.setTitle(id, trimmed, isCustom = true)
        }
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun delete(scan: ScanEntity) = dao.delete(scan.id)

    suspend fun restore(scan: ScanEntity) = dao.restore(scan)

    suspend fun clearAll() = dao.deleteAll()

    suspend fun exportJson(): String = HistoryBackup.toJson(dao.getAll())

    suspend fun exportCsv(): String = HistoryBackup.toCsv(dao.getAll())

    /** Adds scans from a JSON export, skipping codes already saved. Returns how many were added. */
    suspend fun importJson(json: String): Int {
        val scans = HistoryBackup.fromJson(json).map { it.copy(id = 0) }
        return dao.insertIfNew(scans).count { it != -1L }
    }
}
