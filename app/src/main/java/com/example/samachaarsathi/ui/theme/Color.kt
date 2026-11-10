package com.example.samachaarsathi.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.samachaarsathi.data.model.VerdictCategory

// --- Core Palette Tokens (Futuristic Premium Technology) ---
val DeepSpaceBlack = Color(0xFF060911)
val CyberDarkSurface = Color(0xFF0D1424)
val CyberElevatedCard = Color(0xFF131D31)
val CyberCardBorder = Color(0xFF223553)

val ElectricCyan = Color(0xFF00E5FF)
val NeonCobalt = Color(0xFF2979FF)
val LaserBlue = Color(0xFF00B0FF)
val SubtleCyanGlow = Color(0x3300E5FF)

val NeonEmerald = Color(0xFF00E676)
val NeonAmber = Color(0xFFFFD600)
val NeonCrimson = Color(0xFFFF1744)

// Legacy compatibility aliases
val NavyDark = DeepSpaceBlack
val CharcoalDark = CyberDarkSurface
val BlueAccent = ElectricCyan
val BluePrimaryLight = Color(0xFF006699)
val SoftNeutralLight = Color(0xFFE9EEF6)
val OffWhiteLight = Color(0xFFF7F9FC)

// --- Light Theme Tokens ---
val LightPrimary = Color(0xFF006699)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFC7E7FF)
val LightOnPrimaryContainer = Color(0xFF001E30)

val LightSecondary = Color(0xFF3B6488)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFCEE5FF)
val LightOnSecondaryContainer = Color(0xFF001D33)

val LightTertiary = Color(0xFF2F6370)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFD3ECF4)
val LightOnTertiaryContainer = Color(0xFF0C2932)

val LightBackground = Color(0xFFF7F9FC)
val LightOnBackground = Color(0xFF0A101D)

val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0A101D)
val LightSurfaceVariant = Color(0xFFE5EDF7)
val LightOnSurfaceVariant = Color(0xFF3D4B5D)

val LightOutline = Color(0xFF6F7E94)
val LightOutlineVariant = Color(0xFFC4D1E3)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// --- Dark Theme Tokens (Futuristic Void & Cyber Luminescence) ---
val DarkPrimary = Color(0xFF00E5FF)
val DarkOnPrimary = Color(0xFF003640)
val DarkPrimaryContainer = Color(0xFF004E5C)
val DarkOnPrimaryContainer = Color(0xFFB8F5FF)

val DarkSecondary = Color(0xFF82B1FF)
val DarkOnSecondary = Color(0xFF002B73)
val DarkSecondaryContainer = Color(0xFF143B80)
val DarkOnSecondaryContainer = Color(0xFFD6E4FF)

val DarkTertiary = Color(0xFF80D8FF)
val DarkOnTertiary = Color(0xFF003548)
val DarkTertiaryContainer = Color(0xFF004D67)
val DarkOnTertiaryContainer = Color(0xFFBFE9FF)

val DarkBackground = Color(0xFF060911)
val DarkOnBackground = Color(0xFFF0F4FC)

val DarkSurface = Color(0xFF0D1424)
val DarkOnSurface = Color(0xFFF0F4FC)
val DarkSurfaceVariant = Color(0xFF131D31)
val DarkOnSurfaceVariant = Color(0xFFB5C4DA)

val DarkOutline = Color(0xFF425675)
val DarkOutlineVariant = Color(0xFF223553)

val DarkError = Color(0xFFFF5252)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// --- Futuristic Brushes & Gradients ---
object FuturisticGradients {
    val cyanToCobalt = Brush.linearGradient(
        colors = listOf(ElectricCyan, NeonCobalt)
    )

    val cyberCardBorder = Brush.linearGradient(
        colors = listOf(
            ElectricCyan.copy(alpha = 0.5f),
            NeonCobalt.copy(alpha = 0.2f),
            Color.Transparent,
        )
    )

    val subtleBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A4368),
            Color(0xFF182840),
        )
    )

    val ambientGlow = Brush.radialGradient(
        colors = listOf(
            ElectricCyan.copy(alpha = 0.20f),
            NeonCobalt.copy(alpha = 0.08f),
            Color.Transparent,
        )
    )

    val glassSurface = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF142036).copy(alpha = 0.90f),
            Color(0xFF0D1526).copy(alpha = 0.95f),
        )
    )
}

// --- Semantic Verdict Colors (Futuristic Neon) ---
@Immutable
data class VerdictColorSet(
    val main: Color,
    val container: Color,
    val onContainer: Color,
    val border: Color,
)

object VerdictColors {
    // Light
    val SupportedLight = VerdictColorSet(
        main = Color(0xFF00897B),
        container = Color(0xFFE0F2F1),
        onContainer = Color(0xFF003830),
        border = Color(0xFF80CBC4),
    )
    val ContradictedLight = VerdictColorSet(
        main = Color(0xFFD32F2F),
        container = Color(0xFFFFEBEE),
        onContainer = Color(0xFF5C1010),
        border = Color(0xFFEF9A9A),
    )
    val MisleadingLight = VerdictColorSet(
        main = Color(0xFFE65100),
        container = Color(0xFFFFF3E0),
        onContainer = Color(0xFF782500),
        border = Color(0xFFFFCC80),
    )
    val UnverifiedLight = VerdictColorSet(
        main = Color(0xFF0288D1),
        container = Color(0xFFE1F5FE),
        onContainer = Color(0xFF013654),
        border = Color(0xFF81D4FA),
    )

    // Dark (Futuristic Neon Luminescence)
    val SupportedDark = VerdictColorSet(
        main = NeonEmerald,
        container = Color(0xFF082618),
        onContainer = Color(0xFFB9F6CA),
        border = NeonEmerald.copy(alpha = 0.55f),
    )
    val ContradictedDark = VerdictColorSet(
        main = NeonCrimson,
        container = Color(0xFF330911),
        onContainer = Color(0xFFFFCDD2),
        border = NeonCrimson.copy(alpha = 0.60f),
    )
    val MisleadingDark = VerdictColorSet(
        main = NeonAmber,
        container = Color(0xFF332500),
        onContainer = Color(0xFFFFF9C4),
        border = NeonAmber.copy(alpha = 0.55f),
    )
    val UnverifiedDark = VerdictColorSet(
        main = LaserBlue,
        container = Color(0xFF092036),
        onContainer = Color(0xFFB3E5FC),
        border = LaserBlue.copy(alpha = 0.50f),
    )

    fun forCategory(category: VerdictCategory, isDark: Boolean): VerdictColorSet {
        return when (category) {
            VerdictCategory.SUPPORTED -> if (isDark) SupportedDark else SupportedLight
            VerdictCategory.CONTRADICTED -> if (isDark) ContradictedDark else ContradictedLight
            VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> if (isDark) MisleadingDark else MisleadingLight
            VerdictCategory.UNVERIFIED -> if (isDark) UnverifiedDark else UnverifiedLight
        }
    }
}
