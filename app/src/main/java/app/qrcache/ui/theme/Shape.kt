package app.qrcache.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Corner radii and spacing from docs/DESIGN.md. */
object Radius {
    val button = RoundedCornerShape(10.dp)
    val card = RoundedCornerShape(12.dp)
    val sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val viewfinder = 24.dp
}

object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Side padding for every screen. */
    val screen = l
}
