package app.qrcache.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.qrcache.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight, tracking: Double = -0.01) =
    TextStyle(
        fontFamily = Inter,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = tracking.em,
    )

/** Type scale from docs/DESIGN.md, mapped onto Material roles so components pick it up. */
val QRCacheTypography = Typography(
    displaySmall = style(34, 41, FontWeight.Bold, tracking = -0.02), // Large title
    headlineMedium = style(34, 41, FontWeight.Bold, tracking = -0.02), // Large top bar (expanded)
    titleLarge = style(20, 25, FontWeight.SemiBold), // Title
    titleMedium = style(17, 22, FontWeight.SemiBold), // Headline
    bodyLarge = style(17, 22, FontWeight.Normal), // Body
    bodyMedium = style(15, 20, FontWeight.Normal), // Subhead
    bodySmall = style(13, 18, FontWeight.Normal, tracking = 0.0), // Footnote
    labelLarge = style(17, 22, FontWeight.SemiBold), // Buttons
    labelMedium = style(12, 16, FontWeight.Medium, tracking = 0.0), // Caption
    labelSmall = style(12, 16, FontWeight.Medium, tracking = 0.0), // Tab labels
)
