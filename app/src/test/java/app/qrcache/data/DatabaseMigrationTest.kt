package app.qrcache.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.qrcache.data.db.QRCacheDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** History saved by v1.0 (database version 1) must survive the update. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @Test fun version1HistoryOpensAsQrCodes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)

        // The table exactly as v1.0 created it, with one saved scan.
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `scans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`raw` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                    "`titleIsCustom` INTEGER NOT NULL, `firstScannedAt` INTEGER NOT NULL, " +
                    "`lastScannedAt` INTEGER NOT NULL, `scanCount` INTEGER NOT NULL, `favorite` INTEGER NOT NULL)",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_scans_raw` ON `scans` (`raw`)")
            db.execSQL(
                "INSERT INTO scans (raw, type, title, titleIsCustom, firstScannedAt, lastScannedAt, scanCount, favorite) " +
                    "VALUES ('https://menu.hotel.com', 'LINK', 'Breakfast', 1, 1000, 2000, 3, 1)",
            )
            db.version = 1
        }

        val room = Room.databaseBuilder(context, QRCacheDatabase::class.java, name)
            .addMigrations(QRCacheDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        try {
            val scans = room.scanDao().getAll()
            assertEquals(1, scans.size)
            assertEquals("Breakfast", scans[0].title)
            assertEquals(3, scans[0].scanCount)
            assertFalse(scans[0].barcode)
        } finally {
            room.close()
            context.deleteDatabase(name)
        }
    }
}
