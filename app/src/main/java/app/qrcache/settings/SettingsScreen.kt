package app.qrcache.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.qrcache.BuildConfig
import app.qrcache.R
import app.qrcache.ui.components.LargeTitleScaffold
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing

private const val PRIVACY_URL = "https://github.com/Eros-Byrne/QRCache/blob/main/docs/PRIVACY.md"

/** Settings tab. Real settings (theme, haptics, export) arrive in v0.4. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    LargeTitleScaffold(title = stringResource(R.string.tab_settings), modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen),
        ) {
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
}

@Composable
private fun SettingsGroup(header: String, content: @Composable () -> Unit) {
    val colors = QRCacheTheme.colors
    Text(
        header,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
        modifier = Modifier.padding(start = Spacing.l, top = Spacing.l, bottom = Spacing.s),
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
private fun SettingsRow(label: String, value: String? = null, onClick: (() -> Unit)? = null) {
    val colors = QRCacheTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        Spacer(Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.separator,
            )
        }
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
private fun SettingsScreenPreview() {
    QRCacheTheme { SettingsScreen() }
}
