package app.qrcache.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.qrcache.ui.theme.Motion
import app.qrcache.ui.theme.Spacing

/**
 * iOS-style segmented control: a pill track with the selected option on a lighter thumb.
 * With [fillWidth] the segments share the full width equally; otherwise each fits its label.
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    trackColor: Color,
    thumbColor: Color,
    textColor: Color,
    selectedTextColor: Color,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
) {
    Row(
        modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(36.dp)
            .background(trackColor, CircleShape)
            .padding(3.dp)
            .selectableGroup(),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val background by animateColorAsState(
                targetValue = if (isSelected) thumbColor else Color.Transparent,
                animationSpec = Motion.spring(),
                label = "segmentBackground",
            )
            val content by animateColorAsState(
                targetValue = if (isSelected) selectedTextColor else textColor,
                animationSpec = Motion.spring(),
                label = "segmentContent",
            )
            Box(
                Modifier
                    .then(if (fillWidth) Modifier.weight(1f) else Modifier)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(background)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                    .padding(horizontal = Spacing.l),
                contentAlignment = Alignment.Center,
            ) {
                Text(label(option), style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
            }
        }
    }
}
