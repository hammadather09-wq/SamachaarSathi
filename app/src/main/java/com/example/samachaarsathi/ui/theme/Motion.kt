package com.example.samachaarsathi.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

/**
 * Standardized motion tokens and animation specifications for SamachaarSathi,
 * adhering to the Premium Technology design system and native Android ergonomics.
 */
object SamachaarMotion {

    // Standard Durations (milliseconds)
    const val DURATION_MICRO: Int = 120
    const val DURATION_FAST: Int = 200
    const val DURATION_MEDIUM: Int = 280
    const val DURATION_LONG: Int = 380

    // Standard Easing Curves
    val StandardEasing: Easing = FastOutSlowInEasing
    val DecelerateEasing: Easing = LinearOutSlowInEasing
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Standard Animation Specs
    fun <T> fastTween() = tween<T>(durationMillis = DURATION_FAST, easing = StandardEasing)
    fun <T> mediumTween() = tween<T>(durationMillis = DURATION_MEDIUM, easing = StandardEasing)
    fun <T> microTween() = tween<T>(durationMillis = DURATION_MICRO, easing = DecelerateEasing)

    // Spring Configurations
    fun <T> bouncySpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    fun <T> responsiveSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /**
     * Standard screen entrance and exit transitions.
     */
    val ScreenFadeIn = fadeIn(animationSpec = tween(DURATION_MEDIUM, easing = DecelerateEasing))
    val ScreenFadeOut = fadeOut(animationSpec = tween(DURATION_FAST, easing = StandardEasing))

    /**
     * Subtle horizontal slide with fade for tabs and sequential views.
     */
    fun slideInFromRight() = slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() },
        animationSpec = tween(DURATION_MEDIUM, easing = DecelerateEasing),
    ) + fadeIn(animationSpec = tween(DURATION_FAST))

    fun slideOutToLeft() = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
        animationSpec = tween(DURATION_MEDIUM, easing = StandardEasing),
    ) + fadeOut(animationSpec = tween(DURATION_FAST))

    fun slideInFromLeft() = slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
        animationSpec = tween(DURATION_MEDIUM, easing = DecelerateEasing),
    ) + fadeIn(animationSpec = tween(DURATION_FAST))

    fun slideOutToRight() = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() },
        animationSpec = tween(DURATION_MEDIUM, easing = StandardEasing),
    ) + fadeOut(animationSpec = tween(DURATION_FAST))

    /**
     * Helper to detect if user has requested reduced motion or transition animations disabled in system settings.
     */
    @Composable
    fun isReducedMotionEnabled(): Boolean {
        val context = LocalContext.current
        return try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f,
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }
}
