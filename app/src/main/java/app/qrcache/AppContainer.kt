package app.qrcache

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.qrcache.data.ScanRepository
import app.qrcache.data.db.QRCacheDatabase

/** Creates the app's long-lived objects once. Manual dependency injection. */
class AppContainer(context: Context) {
    private val database by lazy { QRCacheDatabase.create(context) }
    val scanRepository by lazy { ScanRepository(database.scanDao()) }
}

/** The shared [ScanRepository], for screens. */
@Composable
fun scanRepository(): ScanRepository =
    (LocalContext.current.applicationContext as QRCacheApplication).container.scanRepository
