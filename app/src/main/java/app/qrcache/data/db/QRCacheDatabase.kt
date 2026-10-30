package app.qrcache.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanEntity::class], version = 1, exportSchema = true)
abstract class QRCacheDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        const val NAME = "qrcache.db"

        fun create(context: Context): QRCacheDatabase =
            Room.databaseBuilder(context, QRCacheDatabase::class.java, NAME).build()
    }
}
