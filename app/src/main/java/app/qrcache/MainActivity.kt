package app.qrcache

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qrcache.data.AppSettings
import app.qrcache.data.ThemeMode
import app.qrcache.ui.theme.QRCacheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settingsRepository = (application as QRCacheApplication).container.settingsRepository
        setContent {
            // Wait for saved settings before the first frame, so a chosen theme never flashes the wrong one.
            val settings by settingsRepository.settings.collectAsStateWithLifecycle<AppSettings?>(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            settings?.let { loaded ->
                val dark = when (loaded.theme) {
                    ThemeMode.SYSTEM -> systemDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
                QRCacheTheme(darkTheme = dark) {
                    QRCacheApp()
                }
            }
        }
    }
}
