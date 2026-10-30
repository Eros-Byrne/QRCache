package app.qrcache.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.qrcache.BuildConfig
import app.qrcache.R
import app.qrcache.appSettings
import app.qrcache.data.AppSettings
import app.qrcache.data.ThemeMode
import app.qrcache.scanRepository
import app.qrcache.settingsRepository
import app.qrcache.ui.components.LargeTitleScaffold
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val PRIVACY_URL = "https://github.com/Eros-Byrne/QRCache/blob/main/docs/PRIVACY.md"

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val settingsRepo = settingsRepository()
    val scans = scanRepository()
    val settings = appSettings()
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    fun say(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    val exportedMsg = stringResource(R.string.settings_exported)
    val exportFailedMsg = stringResource(R.string.settings_export_failed)
    val importFailedMsg = stringResource(R.string.settings_import_failed)
    val clearedMsg = stringResource(R.string.settings_cleared)

    fun export(uri: Uri?, produce: suspend () -> String) {
        if (uri == null) return
        scope.launch {
            val ok = runCatching { writeText(context, uri, produce()) }.isSuccess
            say(if (ok) exportedMsg else exportFailedMsg)
        }
    }

    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        export(it) { scans.exportJson() }
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) {
        export(it) { scans.exportCsv() }
    }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val added = runCatching { scans.importJson(readText(context, uri)) }.getOrNull()
            say(
                if (added == null) importFailedMsg
                else context.resources.getQuantityString(R.plurals.settings_imported, added, added),
            )
        }
    }

    LargeTitleScaffold(
        title = stringResource(R.string.tab_settings),
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen)
                .padding(bottom = Spacing.xl),
        ) {
            SettingsGroup(header = stringResource(R.string.settings_appearance)) {
                ThemePicker(
                    selected = settings.theme,
                    onSelect = { scope.launch { settingsRepo.setTheme(it) } },
                    modifier = Modifier.padding(Spacing.m),
                )
            }

            SettingsGroup(header = stringResource(R.string.settings_scanning)) {
                ToggleRow(
                    label = stringResource(R.string.settings_haptics),
                    checked = settings.haptics,
                    onCheckedChange = { scope.launch { settingsRepo.setHaptics(it) } },
                )
                RowDivider()
                ToggleRow(
                    label = stringResource(R.string.settings_auto_open),
                    detail = stringResource(R.string.settings_auto_open_detail),
                    checked = settings.autoOpenLinks,
                    onCheckedChange = { scope.launch { settingsRepo.setAutoOpenLinks(it) } },
                )
                RowDivider()
                ToggleRow(
                    label = stringResource(R.string.settings_in_app_browser),
                    detail = stringResource(R.string.settings_in_app_browser_detail),
                    checked = settings.inAppBrowser,
                    onCheckedChange = { scope.launch { settingsRepo.setInAppBrowser(it) } },
                )
                RowDivider()
                ToggleRow(
                    label = stringResource(R.string.settings_fetch_titles),
                    detail = stringResource(R.string.settings_fetch_titles_detail),
                    checked = settings.fetchTitles,
                    onCheckedChange = { scope.launch { settingsRepo.setFetchTitles(it) } },
                )
            }

            SettingsGroup(header = stringResource(R.string.settings_history)) {
                val date = LocalDate.now()
                SettingsRow(
                    label = stringResource(R.string.settings_export_json),
                    onClick = { exportJson.launch("qrcache-history-$date.json") },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.settings_export_csv),
                    onClick = { exportCsv.launch("qrcache-history-$date.csv") },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.settings_import),
                    onClick = { importFile.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.settings_clear),
                    labelColor = QRCacheTheme.colors.danger,
                    showChevron = false,
                    onClick = { confirmClear = true },
                )
            }

            SettingsGroup(header = stringResource(R.string.settings_about)) {
                SettingsRow(
                    label = stringResource(R.string.settings_version),
                    value = BuildConfig.VERSION_NAME,
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.settings_privacy),
                    onClick = { uriHandler.openUri(PRIVACY_URL) },
                )
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.settings_clear_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        scope.launch {
                            scans.clearAll()
                            say(clearedMsg)
                        }
                    },
                ) { Text(stringResource(R.string.settings_clear_confirm), color = QRCacheTheme.colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = QRCacheTheme.colors.surface,
        )
    }
}

private suspend fun writeText(context: Context, uri: Uri, text: String) = withContext(Dispatchers.IO) {
    val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("Can't open $uri")
    stream.bufferedWriter().use { it.write(text) }
}

private suspend fun readText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val stream = context.contentResolver.openInputStream(uri) ?: error("Can't open $uri")
    stream.bufferedReader().use { it.readText() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    val colors = QRCacheTheme.colors
    val options = listOf(
        ThemeMode.SYSTEM to R.string.settings_theme_system,
        ThemeMode.LIGHT to R.string.settings_theme_light,
        ThemeMode.DARK to R.string.settings_theme_dark,
    )
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.accent,
                    activeContentColor = Color.White,
                    inactiveContainerColor = colors.surfaceSecondary,
                    inactiveContentColor = colors.textPrimary,
                    activeBorderColor = Color.Transparent,
                    inactiveBorderColor = Color.Transparent,
                ),
            ) {
                Text(stringResource(label))
            }
        }
    }
}

@Composable
private fun SettingsGroup(header: String, content: @Composable () -> Unit) {
    val colors = QRCacheTheme.colors
    Text(
        header,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
        modifier = Modifier.padding(start = Spacing.l, top = Spacing.xl, bottom = Spacing.s),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, Radius.card),
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    labelColor: Color = QRCacheTheme.colors.textPrimary,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = QRCacheTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
        }
        if (onClick != null && showChevron) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.separator,
            )
        }
    }
}

/** iOS-style switch row: the whole row is tappable, the switch turns green when on. */
@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, detail: String? = null) {
    val colors = QRCacheTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.width(Spacing.m))
        Switch(
            checked = checked,
            onCheckedChange = null, // the row handles taps, so TalkBack reads one control
            colors = SwitchDefaults.colors(
                checkedTrackColor = colors.success,
                checkedThumbColor = Color.White,
                checkedBorderColor = Color.Transparent,
                uncheckedTrackColor = colors.surfaceSecondary,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = Spacing.l),
        thickness = 0.5.dp,
        color = QRCacheTheme.colors.separator,
    )
}

@Preview
@Composable
private fun ToggleRowPreview() {
    QRCacheTheme { ToggleRow(label = "Vibrate on scan", checked = AppSettings().haptics, onCheckedChange = {}) }
}
