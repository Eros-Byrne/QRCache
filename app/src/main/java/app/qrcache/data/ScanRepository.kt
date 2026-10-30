package app.qrcache.data

import app.qrcache.data.db.ScanDao
import app.qrcache.data.db.ScanEntity
import app.qrcache.scan.ContentParser
import kotlinx.coroutines.flow.Flow

/** The only way the app reads or writes scan history. */
class ScanRepository(
    private val dao: ScanDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<ScanEntity>> = dao.observeAll()

    fun observe(id: Long): Flow<ScanEntity?> = dao.observe(id)

    /** Saves a freshly scanned code and returns its id. */
    suspend fun recordScan(raw: String): Long {
        val content = ContentParser.parse(raw)
        return dao.recordScan(raw, content.scanType, content.defaultTitle, clock())
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
}
