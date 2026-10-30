package app.qrcache.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qrcache.R
import app.qrcache.data.db.ScanEntity
import app.qrcache.detail.ScanDetailSheet
import app.qrcache.scan.icon
import app.qrcache.scanRepository
import app.qrcache.ui.components.EmptyState
import app.qrcache.ui.components.LargeTitleScaffold
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** History tab: every saved scan, grouped by day, with search and filters. */
@Composable
fun HistoryScreen(onScanClick: () -> Unit, modifier: Modifier = Modifier) {
    val repository = scanRepository()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val scans by remember { repository.observeAll() }.collectAsStateWithLifecycle(initialValue = null)
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    var openId by rememberSaveable { mutableStateOf<Long?>(null) }

    val deletedMessage = stringResource(R.string.history_deleted)
    val undoLabel = stringResource(R.string.undo)
    fun delete(scan: ScanEntity, alreadyDeleted: Boolean = false) {
        scope.launch {
            if (!alreadyDeleted) repository.delete(scan)
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(deletedMessage, undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) repository.restore(scan)
        }
    }

    LargeTitleScaffold(
        title = stringResource(R.string.tab_history),
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val all = scans
        when {
            all == null -> Unit // first load from the database takes a few milliseconds
            all.isEmpty() -> Box(
                Modifier
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
            else -> HistoryList(
                scans = all,
                query = query,
                onQueryChange = { query = it },
                filter = filter,
                onFilterChange = { filter = it },
                onOpen = { openId = it.id },
                onFavorite = { scope.launch { repository.setFavorite(it.id, !it.favorite) } },
                onDelete = { delete(it) },
                contentPadding = padding,
            )
        }
    }

    openId?.let { id ->
        ScanDetailSheet(
            scanId = id,
            onDismiss = { openId = null },
            onDeleted = { delete(it, alreadyDeleted = true) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    scans: List<ScanEntity>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    onOpen: (ScanEntity) -> Unit,
    onFavorite: (ScanEntity) -> Unit,
    onDelete: (ScanEntity) -> Unit,
    contentPadding: PaddingValues,
) {
    val zone = remember { ZoneId.systemDefault() }
    val today = LocalDate.now(zone)
    val groups = remember(scans, query, filter, today) {
        scans.filterHistory(query, filter).groupBySection(today, zone)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + Spacing.xl,
        ),
    ) {
        item(key = "search") {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.s),
            )
        }
        item(key = "filters") {
            FilterChips(selected = filter, onSelect = onFilterChange)
        }
        if (groups.isEmpty()) {
            item(key = "no-matches") {
                Text(
                    stringResource(R.string.history_no_matches),
                    style = MaterialTheme.typography.bodyMedium,
                    color = QRCacheTheme.colors.textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxl),
                )
            }
        }
        groups.forEach { (section, sectionScans) ->
            item(key = "header-$section") {
                Text(
                    stringResource(section.label),
                    style = MaterialTheme.typography.bodySmall,
                    color = QRCacheTheme.colors.textSecondary,
                    modifier = Modifier
                        .animateItem()
                        .padding(start = Spacing.screen + Spacing.l, top = Spacing.l, bottom = Spacing.s),
                )
            }
            items(sectionScans, key = { it.id }) { scan ->
                val isFirst = scan.id == sectionScans.first().id
                val isLast = scan.id == sectionScans.last().id
                SwipeableHistoryRow(
                    scan = scan,
                    timeLabel = timeLabel(scan.lastScannedAt, today, zone),
                    isFirst = isFirst,
                    isLast = isLast,
                    onClick = { onOpen(scan) },
                    onFavorite = { onFavorite(scan) },
                    onDelete = { onDelete(scan) },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = Spacing.screen),
                )
            }
        }
    }
}

private val HistorySection.label: Int
    get() = when (this) {
        HistorySection.TODAY -> R.string.history_section_today
        HistorySection.YESTERDAY -> R.string.history_section_yesterday
        HistorySection.THIS_WEEK -> R.string.history_section_this_week
        HistorySection.EARLIER -> R.string.history_section_earlier
    }

private val HistoryFilter.label: Int
    get() = when (this) {
        HistoryFilter.ALL -> R.string.filter_all
        HistoryFilter.FAVORITES -> R.string.filter_favorites
        HistoryFilter.LINKS -> R.string.filter_links
        HistoryFilter.WIFI -> R.string.filter_wifi
        HistoryFilter.TEXT -> R.string.filter_text
        HistoryFilter.OTHER -> R.string.filter_other
    }

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = QRCacheTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .background(colors.surfaceSecondary, CircleShape)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.s))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.history_search), style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.Cancel,
                    contentDescription = stringResource(R.string.history_search_clear),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterChips(selected: HistoryFilter, onSelect: (HistoryFilter) -> Unit) {
    val colors = QRCacheTheme.colors
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        HistoryFilter.entries.forEach { f ->
            FilterChip(
                selected = f == selected,
                onClick = { onSelect(f) },
                label = { Text(stringResource(f.label)) },
                shape = CircleShape,
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surfaceSecondary,
                    labelColor = colors.textPrimary,
                    selectedContainerColor = colors.accent,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

/** Swipe left to delete, swipe right to favorite (the row snaps back). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHistoryRow(
    scan: ScanEntity,
    timeLabel: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QRCacheTheme.colors
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 12.dp else 0.dp,
        topEnd = if (isFirst) 12.dp else 0.dp,
        bottomStart = if (isLast) 12.dp else 0.dp,
        bottomEnd = if (isLast) 12.dp else 0.dp,
    )
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onFavorite()
                    false
                }
                SwipeToDismissBoxValue.Settled -> true
            }
        },
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier.clip(shape),
        backgroundContent = {
            val toDelete = state.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val iconScale by animateFloatAsState(if (state.targetValue != SwipeToDismissBoxValue.Settled) 1.15f else 1f, label = "swipeIcon")
            Box(
                Modifier
                    .fillMaxSize()
                    .background(if (toDelete) colors.danger else colors.accent)
                    .padding(horizontal = Spacing.xl),
                contentAlignment = if (toDelete) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                Icon(
                    if (toDelete) Icons.Rounded.Delete else Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                )
            }
        },
    ) {
        Column(Modifier.background(colors.surface)) {
            HistoryRow(scan = scan, timeLabel = timeLabel, onClick = onClick)
            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = Spacing.l + 36.dp + Spacing.m),
                    thickness = 0.5.dp,
                    color = colors.separator,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(scan: ScanEntity, timeLabel: String, onClick: () -> Unit) {
    val colors = QRCacheTheme.colors
    val content = remember(scan.raw) { scan.content }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(colors.accent.copy(alpha = 0.12f), Radius.button),
            contentAlignment = Alignment.Center,
        ) {
            Icon(content.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Spacing.m))
        Column(Modifier.weight(1f)) {
            Text(
                scan.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val preview = scan.preview()
            if (preview.isNotEmpty() && preview != scan.title) {
                Text(
                    preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(Spacing.s))
        if (scan.favorite) {
            Icon(Icons.Rounded.Star, contentDescription = stringResource(R.string.filter_favorites), tint = colors.accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(timeLabel, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}
