package app.qrcache.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.qrcache.data.ScanType
import app.qrcache.scan.ContentParser
import app.qrcache.scan.ScanContent

/** One saved scan. Times are epoch millis. */
@Entity(tableName = "scans", indices = [Index(value = ["raw"], unique = true)])
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Exact decoded text. Unique: rescanning updates the existing row. */
    val raw: String,
    val type: ScanType,
    /** Display name; the user can rename it. */
    val title: String,
    val titleIsCustom: Boolean = false,
    val firstScannedAt: Long,
    val lastScannedAt: Long,
    val scanCount: Int = 1,
    val favorite: Boolean = false,
) {
    val content: ScanContent get() = ContentParser.parse(raw)
}
