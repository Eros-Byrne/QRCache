package app.qrcache.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius

/** Full-width 50dp accent button. One per screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .pressScale(interactionSource),
        shape = Radius.card,
        colors = ButtonDefaults.buttonColors(
            containerColor = QRCacheTheme.colors.accent,
            contentColor = Color.White,
        ),
        elevation = null,
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    QRCacheTheme { PrimaryButton(text = "Scan your first code", onClick = {}) }
}
