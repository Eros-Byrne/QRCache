package app.qrcache.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.qrcache.data.ScanType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ScanDao {

    @Query("SELECT * FROM scans ORDER BY lastScannedAt DESC")
    abstract fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    abstract fun observe(id: Long): Flow<ScanEntity?>

    @Query("SELECT * FROM scans WHERE id = :id")
    abstract suspend fun get(id: Long): ScanEntity?

    @Query("SELECT * FROM scans WHERE raw = :raw LIMIT 1")
    abstract suspend fun findByRaw(raw: String): ScanEntity?

    @Insert
    abstract suspend fun insert(scan: ScanEntity): Long

    @Update
    abstract suspend fun update(scan: ScanEntity)

    /** Puts a deleted scan back exactly as it was (Undo). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun restore(scan: ScanEntity)

    @Query("UPDATE scans SET title = :title, titleIsCustom = :isCustom WHERE id = :id")
    abstract suspend fun setTitle(id: Long, title: String, isCustom: Boolean)

    @Query("UPDATE scans SET favorite = :favorite WHERE id = :id")
    abstract suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM scans WHERE id = :id")
    abstract suspend fun delete(id: Long)

    /**
     * Saves a scan. If the same code was scanned before, moves it to the top and bumps its
     * count instead of adding a copy. Returns the scan's id.
     */
    @Transaction
    open suspend fun recordScan(raw: String, type: ScanType, defaultTitle: String, now: Long): Long {
        val existing = findByRaw(raw)
        if (existing != null) {
            update(existing.copy(lastScannedAt = now, scanCount = existing.scanCount + 1))
            return existing.id
        }
        return insert(
            ScanEntity(raw = raw, type = type, title = defaultTitle, firstScannedAt = now, lastScannedAt = now),
        )
    }
}
