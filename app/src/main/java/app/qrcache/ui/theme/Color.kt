package app.qrcache.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Color tokens from docs/DESIGN.md. Screens use these, never raw hex values. */
@Immutable
data class QRCacheColors(
    val isDark: Boolean,
    val accent: Color,
    val success: Color,
    val danger: Color,
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val separator: Color,
)

val LightColors = QRCacheColors(
    isDark = false,
    accent = Color(0xFF0A84FF),
    success = Color(0xFF30D158),
    danger = Color(0xFFFF3B30),
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFE5E5EA),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF6C6C70),
    separator = Color(0xFFC6C6C8),
)

val DarkColors = QRCacheColors(
    isDark = true,
    accent = Color(0xFF409CFF),
    success = Color(0xFF32D74B),
    danger = Color(0xFFFF453A),
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceSecondary = Color(0xFF2C2C2E),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFAEAEB2),
    separator = Color(0xFF38383A),
)
