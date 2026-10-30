package app.qrcache.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val haptics: Boolean = true,
    /** Open links right after scanning instead of waiting for a tap. */
    val autoOpenLinks: Boolean = false,
    /** Open links in an in-app browser tab (Custom Tabs) rather than the default browser app. */
    val inAppBrowser: Boolean = true,
)

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val store = context.settingsStore

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val haptics = booleanPreferencesKey("haptics")
        val autoOpenLinks = booleanPreferencesKey("auto_open_links")
        val inAppBrowser = booleanPreferencesKey("in_app_browser")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        val defaults = AppSettings()
        AppSettings(
            theme = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.theme,
            haptics = p[Keys.haptics] ?: defaults.haptics,
            autoOpenLinks = p[Keys.autoOpenLinks] ?: defaults.autoOpenLinks,
            inAppBrowser = p[Keys.inAppBrowser] ?: defaults.inAppBrowser,
        )
    }

    suspend fun setTheme(theme: ThemeMode) = store.edit { it[Keys.theme] = theme.name }
    suspend fun setHaptics(on: Boolean) = store.edit { it[Keys.haptics] = on }
    suspend fun setAutoOpenLinks(on: Boolean) = store.edit { it[Keys.autoOpenLinks] = on }
    suspend fun setInAppBrowser(on: Boolean) = store.edit { it[Keys.inAppBrowser] = on }
}
