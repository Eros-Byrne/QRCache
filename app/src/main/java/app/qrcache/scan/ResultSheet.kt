package app.qrcache.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.qrcache.R
import app.qrcache.ui.components.PrimaryButton
import app.qrcache.ui.components.pressScale
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing

/** Slides up after a scan: what the code is, and what you can do with it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheet(content: ScanContent, onDismiss: () -> Unit) {
    val colors = QRCacheTheme.colors
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = Radius.sheet,
        containerColor = colors.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = Spacing.l),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(colors.accent.copy(alpha = 0.12f), Radius.button),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(content.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(Spacing.m))
                Text(
                    stringResource(content.typeLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.height(Spacing.l))
            Text(
                content.defaultTitle,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Spacing.xs))
            SelectionContainer {
                Text(
                    content.displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Spacing.xl))
            PrimaryButton(
                text = stringResource(content.primaryActionLabel()),
                onClick = { content.runPrimaryAction(context) },
            )
            Spacer(Modifier.height(Spacing.m))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                SecondaryAction(
                    icon = Icons.Rounded.ContentCopy,
                    label = stringResource(R.string.action_copy),
                    onClick = { copyToClipboard(context, content.copyValue()) },
                    modifier = Modifier.weight(1f),
                )
                SecondaryAction(
                    icon = Icons.Rounded.Share,
                    label = stringResource(R.string.action_share),
                    onClick = { share(context, content.raw) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SecondaryAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = QRCacheTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .pressScale(interactionSource),
        shape = Radius.card,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colors.surfaceSecondary,
            contentColor = colors.accent,
        ),
        interactionSource = interactionSource,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.s))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
