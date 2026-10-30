package app.qrcache.detail

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qrcache.R
import app.qrcache.data.db.ScanEntity
import app.qrcache.scan.copyToClipboard
import app.qrcache.scan.copyValue
import app.qrcache.scan.displayValue
import app.qrcache.scan.icon
import app.qrcache.scan.primaryActionLabel
import app.qrcache.scan.runPrimaryAction
import app.qrcache.scan.share
import app.qrcache.scan.typeLabel
import app.qrcache.scanRepository
import app.qrcache.ui.components.PrimaryButton
import app.qrcache.ui.components.pressScale
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * One saved scan: what it is, what you can do with it, and rename / favorite / delete.
 * Used right after scanning and when opening a scan from History.
 * [onDeleted] lets the caller offer Undo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailSheet(
    scanId: Long,
    onDismiss: () -> Unit,
    onDeleted: (ScanEntity) -> Unit = {},
) {
    val repository = scanRepository()
    val scope = rememberCoroutineScope()
    val scan by remember(scanId) { repository.observe(scanId) }.collectAsStateWithLifecycle(initialValue = null)
    var loaded by remember(scanId) { mutableStateOf(false) }
    LaunchedEffect(scan) {
        if (scan != null) loaded = true else if (loaded) onDismiss() // deleted elsewhere
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = Radius.sheet,
        containerColor = QRCacheTheme.colors.surface,
    ) {
        val current = scan
        if (current == null) {
            Spacer(Modifier.height(240.dp)) // keeps the sheet's height while loading
        } else {
            DetailContent(
                scan = current,
                onDelete = {
                    // The sheet closes itself once the scan is gone (see LaunchedEffect above).
                    scope.launch { repository.delete(current) }
                    onDeleted(current)
                },
            )
        }
    }
}

@Composable
private fun DetailContent(scan: ScanEntity, onDelete: () -> Unit) {
    val colors = QRCacheTheme.colors
    val context = LocalContext.current
    val repository = scanRepository()
    val scope = rememberCoroutineScope()
    val content = remember(scan.raw) { scan.content }
    var renaming by rememberSaveable { mutableStateOf(false) }

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
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { scope.launch { repository.setFavorite(scan.id, !scan.favorite) } }) {
                Icon(
                    if (scan.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = stringResource(
                        if (scan.favorite) R.string.action_unfavorite else R.string.action_favorite,
                    ),
                    tint = if (scan.favorite) colors.accent else colors.textSecondary,
                )
            }
            IconButton(onClick = { renaming = true }) {
                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.action_rename), tint = colors.textSecondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.action_delete), tint = colors.danger)
            }
        }
        Spacer(Modifier.height(Spacing.m))
        Text(
            scan.title,
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
        Spacer(Modifier.height(Spacing.s))
        Text(
            scanMeta(scan),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
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
                onClick = { share(context, scan.raw) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (renaming) {
        RenameDialog(
            current = if (scan.titleIsCustom) scan.title else "",
            placeholder = content.defaultTitle,
            onSave = { name ->
                renaming = false
                scope.launch { repository.rename(scan.id, name) }
            },
            onCancel = { renaming = false },
        )
    }
}

/** "Scanned Oct 30, 2026, 8:12 AM · Scanned 3 times" */
@Composable
private fun scanMeta(scan: ScanEntity): String {
    val formatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT) }
    val time = Instant.ofEpochMilli(scan.lastScannedAt).atZone(ZoneId.systemDefault()).format(formatter)
    val scannedAt = stringResource(R.string.detail_scanned_at, time)
    return if (scan.scanCount > 1) {
        scannedAt + " · " + pluralStringResource(R.plurals.detail_scan_count, scan.scanCount, scan.scanCount)
    } else {
        scannedAt
    }
}

@Composable
private fun RenameDialog(current: String, placeholder: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue(current, selection = TextRange(0, current.length))) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.rename_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingText = { Text(stringResource(R.string.rename_hint)) },
                shape = Radius.card,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text.text) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) } },
        containerColor = QRCacheTheme.colors.surface,
    )
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
