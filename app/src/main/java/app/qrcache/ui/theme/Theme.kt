package app.qrcache.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LocalQRCacheColors = staticCompositionLocalOf { LightColors }

/** Access design tokens: `QRCacheTheme.colors.accent`. */
object QRCacheTheme {
    val colors: QRCacheColors
        @Composable get() = LocalQRCacheColors.current
}

@Composable
fun QRCacheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    // Map our tokens onto Material roles so stock M3 components match the design.
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = colors.accent,
        onPrimary = Color.White,
        error = colors.danger,
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.background,
        onSurface = colors.textPrimary,
        surfaceContainer = colors.surface,
        surfaceContainerLow = colors.surface,
        surfaceContainerHigh = colors.surface,
        surfaceVariant = colors.surfaceSecondary,
        onSurfaceVariant = colors.textSecondary,
        outlineVariant = colors.separator,
    )
    CompositionLocalProvider(LocalQRCacheColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = QRCacheTypography,
            content = content,
        )
    }
}
