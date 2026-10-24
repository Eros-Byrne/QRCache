package app.qrcache.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.qrcache.R
import app.qrcache.ui.components.EmptyState
import app.qrcache.ui.components.LargeTitleScaffold
import app.qrcache.ui.theme.QRCacheTheme

/** History tab. Shows the empty state until saving scans lands in v0.3. */
@Composable
fun HistoryScreen(onScanClick: () -> Unit, modifier: Modifier = Modifier) {
    LargeTitleScaffold(title = stringResource(R.string.tab_history), modifier = modifier) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                icon = Icons.Rounded.History,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
                actionLabel = stringResource(R.string.history_empty_action),
                onAction = onScanClick,
            )
        }
    }
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    QRCacheTheme { HistoryScreen(onScanClick = {}) }
}
