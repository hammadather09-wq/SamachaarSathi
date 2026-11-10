package com.example.samachaarsathi.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.LaserBlue
import com.example.samachaarsathi.ui.theme.NeonCobalt
import com.example.samachaarsathi.ui.theme.SamachaarMotion

/**
 * Operational state of the AI Truth Halo engine.
 * Reflects reality honestly: never falsely presents an active verification state unless executing.
 */
enum class TruthHaloState {
    IDLE,
    INPUT_ACTIVE,
    VERIFYING,
}

/**
 * Visual centerpiece holographic Truth Halo orb inspired by cyber intelligence references.
 * Features multi-layered orbital light filaments, smooth volumetric back-glow,
 * and adaptive rotation speeds matching state without false verification claims.
 */
@Composable
fun AiTruthHaloCenterpiece(
    state: TruthHaloState = TruthHaloState.IDLE,
    modifier: Modifier = Modifier,
    size: Dp = 124.dp,
) {
    val isReducedMotion = SamachaarMotion.isReducedMotionEnabled()
    val isDark = isSystemInDarkTheme()

    val rotationDuration = when (state) {
        TruthHaloState.IDLE -> 14000
        TruthHaloState.INPUT_ACTIVE -> 9000
        TruthHaloState.VERIFYING -> 4000
    }

    val pulseDuration = when (state) {
        TruthHaloState.IDLE -> 3200
        TruthHaloState.INPUT_ACTIVE -> 2400
        TruthHaloState.VERIFYING -> 1200
    }

    val pulseTarget = when (state) {
        TruthHaloState.IDLE -> 1.04f
        TruthHaloState.INPUT_ACTIVE -> 1.06f
        TruthHaloState.VERIFYING -> 1.09f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "HaloAnimation")
    val rotation by if (isReducedMotion) {
        rememberInfiniteTransition(label = "Static").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticRotation",
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(rotationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "HaloRotation",
        )
    }

    val pulseScale by if (isReducedMotion) {
        rememberInfiniteTransition(label = "StaticPulse").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticPulse",
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = pulseTarget,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "HaloPulse",
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = (this.size.minDimension / 2) * 0.80f * pulseScale

            // 1. Volumetric Deep Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = if (isDark) 0.38f else 0.22f),
                        NeonCobalt.copy(alpha = if (isDark) 0.20f else 0.10f),
                        LaserBlue.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = this.size.minDimension / 2,
                ),
                radius = this.size.minDimension / 2,
                center = center,
            )

            // 2. Spherical Obsidian Body Depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D1E36).copy(alpha = 0.85f),
                        Color(0xFF040A14).copy(alpha = 0.95f),
                    ),
                    center = center,
                    radius = baseRadius * 0.92f,
                ),
                radius = baseRadius * 0.92f,
                center = center,
            )

            // 3. Primary Rotating Filament Ribbon (Clockwise)
            rotate(rotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricCyan,
                            Color.White.copy(alpha = 0.9f),
                            NeonCobalt,
                            LaserBlue,
                            Color.Transparent,
                            ElectricCyan.copy(alpha = 0.7f),
                        ),
                        center = center,
                    ),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                )
            }

            // 4. Counter-rotating Tilted Elliptical Energy Ring
            rotate(-rotation * 1.4f, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonCobalt.copy(alpha = 0.9f),
                            ElectricCyan,
                            Color.White.copy(alpha = 0.8f),
                            Color.Transparent,
                        ),
                        center = center,
                    ),
                    startAngle = 30f,
                    sweepAngle = 280f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.65f),
                    size = Size(baseRadius * 1.64f, baseRadius * 1.30f),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            // 5. Inner Harmonic Orbit
            rotate(rotation * 0.8f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            LaserBlue.copy(alpha = 0.6f),
                            Color.Transparent,
                            ElectricCyan.copy(alpha = 0.8f),
                            Color.Transparent,
                        ),
                        center = center,
                    ),
                    radius = baseRadius * 0.52f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }

            // 6. Central Radiant Cyan-White Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        ElectricCyan.copy(alpha = 0.80f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = baseRadius * 0.32f,
                ),
                radius = baseRadius * 0.32f,
                center = center,
            )
        }
    }
}

/**
 * Luminous AI Truth Halo hero component with status telemetry pill and branding.
 */
@Composable
fun AiTruthHaloHeader(
    modifier: Modifier = Modifier,
    state: TruthHaloState = TruthHaloState.IDLE,
    title: String = "SamachaarSathi",
    tagline: String = "Check the information before you share it.",
    statusTextOverride: String? = null,
) {
    val isDark = isSystemInDarkTheme()
    val telemetryText = statusTextOverride ?: when (state) {
        TruthHaloState.IDLE -> "TRUTH ENGINE • READY"
        TruthHaloState.INPUT_ACTIVE -> "CONSOLE • READY TO VERIFY"
        TruthHaloState.VERIFYING -> "ANALYZING EVIDENCE"
    }
    val indicatorColor = when (state) {
        TruthHaloState.IDLE -> ElectricCyan
        TruthHaloState.INPUT_ACTIVE -> ElectricCyan
        TruthHaloState.VERIFYING -> NeonCobalt
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Status Telemetry Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF0C1929) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(
                1.dp,
                if (isDark) indicatorColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = telemetryText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor,
                    letterSpacing = 1.2.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Centerpiece Orb
        AiTruthHaloCenterpiece(
            state = state,
            size = 120.dp,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Application Branding
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Tagline
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
