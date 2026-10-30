package app.qrcache.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring as springSpec
import androidx.compose.animation.core.tween

/** Motion tokens from docs/DESIGN.md. */
object Motion {
    /** Default spring for anything that moves. */
    fun <T> spring(): FiniteAnimationSpec<T> = springSpec(dampingRatio = 0.8f, stiffness = 380f)

    /** Tab switch crossfade. */
    fun <T> tabFade(): FiniteAnimationSpec<T> = tween(durationMillis = 180)

    const val PRESSED_SCALE = 0.97f
}
