package app.qrcache

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qrcache.data.AppSettings
import app.qrcache.data.ScanRepository
import app.qrcache.data.SettingsRepository
import app.qrcache.data.TitleFetcher
import app.qrcache.data.db.QRCacheDatabase

/** Creates the app's long-lived objects once. Manual dependency injection. */
class AppContainer(context: Context) {
    private val database by lazy { QRCacheDatabase.create(context) }
    val scanRepository by lazy {
        ScanRepository(
            dao = database.scanDao(),
            titles = TitleFetcher(
                userAgent = "QRCache/${BuildConfig.VERSION_NAME} (Android; +https://github.com/Eros-Byrne/QRCache)",
            ),
        )
    }
    val settingsRepository by lazy { SettingsRepository(context) }
}

@Composable
private fun container(): AppContainer = (LocalContext.current.applicationContext as QRCacheApplication).container

/** The shared [ScanRepository], for screens. */
@Composable
fun scanRepository(): ScanRepository = container().scanRepository

/** The shared [SettingsRepository], for screens. */
@Composable
fun settingsRepository(): SettingsRepository = container().settingsRepository

/** Current settings. Defaults until the first read finishes (a few milliseconds). */
@Composable
fun appSettings(): AppSettings {
    val repository = settingsRepository()
    val settings by remember(repository) { repository.settings }.collectAsStateWithLifecycle(AppSettings())
    return settings
}
