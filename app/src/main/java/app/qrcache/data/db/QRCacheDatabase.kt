package app.qrcache.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ScanEntity::class], version = 2, exportSchema = true)
abstract class QRCacheDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        const val NAME = "qrcache.db"

        /** v2 remembers which scans were barcodes. Existing scans were all QR codes. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scans ADD COLUMN barcode INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun create(context: Context): QRCacheDatabase =
            Room.databaseBuilder(context, QRCacheDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
