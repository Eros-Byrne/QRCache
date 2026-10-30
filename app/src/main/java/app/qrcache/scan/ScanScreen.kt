package app.qrcache.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.qrcache.R
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing

/**
 * Scan tab. For now a dark stand-in where the camera preview will go (v0.2),
 * with the viewfinder from docs/DESIGN.md.
 */
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screen),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Viewfinder(Modifier.size(260.dp))
            Spacer(Modifier.height(Spacing.xxl))
            Text(
                stringResource(R.string.scan_hint),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(Spacing.s))
            Text(
                stringResource(R.string.scan_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Rounded corner brackets only, no full box. */
@Composable
private fun Viewfinder(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val stroke = 5.dp.toPx()
        val radius = Radius.viewfinder.toPx()
        val arm = size.minDimension * 0.22f
        val inset = stroke / 2
        val w = size.width - inset
        val h = size.height - inset
        val arc = Size(radius * 2, radius * 2)

        val path = Path().apply {
            // Top left
            moveTo(inset, inset + arm)
            lineTo(inset, inset + radius)
            arcTo(Rect(Offset(inset, inset), arc), 180f, 90f, false)
            lineTo(inset + arm, inset)
            // Top right
            moveTo(w - arm, inset)
            lineTo(w - radius, inset)
            arcTo(Rect(Offset(w - radius * 2, inset), arc), 270f, 90f, false)
            lineTo(w, inset + arm)
            // Bottom right
            moveTo(w, h - arm)
            lineTo(w, h - radius)
            arcTo(Rect(Offset(w - radius * 2, h - radius * 2), arc), 0f, 90f, false)
            lineTo(w - arm, h)
            // Bottom left
            moveTo(inset + arm, h)
            lineTo(inset + radius, h)
            arcTo(Rect(Offset(inset, h - radius * 2), arc), 90f, 90f, false)
            lineTo(inset, h - arm)
        }
        drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun ScanScreenPreview() {
    QRCacheTheme { ScanScreen() }
}
