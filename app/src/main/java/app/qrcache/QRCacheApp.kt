package app.qrcache

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.qrcache.history.HistoryScreen
import app.qrcache.scan.ScanScreen
import app.qrcache.settings.SettingsScreen
import app.qrcache.ui.theme.Motion
import app.qrcache.ui.theme.QRCacheTheme

enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    Scan(R.string.tab_scan, Icons.Rounded.QrCodeScanner),
    History(R.string.tab_history, Icons.Rounded.History),
    Settings(R.string.tab_settings, Icons.Rounded.Settings),
}

/** App shell: three tabs, opening on Scan. */
@Composable
fun QRCacheApp() {
    var tab by rememberSaveable { mutableStateOf(Tab.Scan) }
    val colors = QRCacheTheme.colors

    Scaffold(
        containerColor = if (tab == Tab.Scan) Color.Black else colors.background,
        bottomBar = { TabBar(selected = tab, onSelect = { tab = it }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Crossfade(targetState = tab, animationSpec = Motion.tabFade(), label = "tab") { current ->
                when (current) {
                    Tab.Scan -> ScanScreen(Modifier.padding(top = padding.calculateTopPadding()))
                    Tab.History -> HistoryScreen(onScanClick = { tab = Tab.Scan })
                    Tab.Settings -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun TabBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val colors = QRCacheTheme.colors
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = colors.separator)
        NavigationBar(
            containerColor = colors.surface.copy(alpha = 0.94f),
            tonalElevation = 0.dp,
        ) {
            Tab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(stringResource(tab.label)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.accent,
                        selectedTextColor = colors.accent,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun QRCacheAppPreview() {
    QRCacheTheme { QRCacheApp() }
}
