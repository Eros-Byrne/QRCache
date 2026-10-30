package app.qrcache.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Text colors must meet WCAG AA (4.5:1) on every surface, as DESIGN.md promises. */
class ColorContrastTest {

    @Test
    fun textMeetsAaOnBackgroundAndSurface() {
        for ((name, colors) in listOf("light" to LightColors, "dark" to DarkColors)) {
            for (text in listOf(colors.textPrimary, colors.textSecondary)) {
                for (bg in listOf(colors.background, colors.surface)) {
                    val ratio = contrast(text, bg)
                    assertTrue("$name: $text on $bg is $ratio:1", ratio >= 4.5)
                }
            }
        }
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun luminance(c: Color): Double {
        fun channel(v: Float): Double =
            if (v <= 0.04045f) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        return 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
    }
}
