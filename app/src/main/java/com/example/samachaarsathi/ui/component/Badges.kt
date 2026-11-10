package com.example.samachaarsathi.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.LaserBlue
import com.example.samachaarsathi.ui.theme.NeonAmber
import com.example.samachaarsathi.ui.theme.NeonEmerald
import com.example.samachaarsathi.ui.theme.VerdictColors

/**
 * Visual badge communicating fact-checking verdict category with futuristic neon luminescence.
 * Pairs color with clear text and icon for accessibility.
 */
@Composable
fun VerdictBadge(
    category: VerdictCategory,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    showIcon: Boolean = true,
) {
    val colorSet = VerdictColors.forCategory(category, isDark)
    val (label: String, icon: ImageVector) = when (category) {
        VerdictCategory.SUPPORTED -> Pair("VERIFIED TRUE", Icons.Default.Check)
        VerdictCategory.CONTRADICTED -> Pair("VERIFIED FALSE", Icons.Default.Close)
        VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> Pair("MISLEADING / CONTEXT NEEDED", Icons.Default.Warning)
        VerdictCategory.UNVERIFIED -> Pair("UNVERIFIED", Icons.Default.Info)
    }

    Surface(
        modifier = modifier.semantics {
            contentDescription = "Verdict: $label"
        },
        shape = RoundedCornerShape(20.dp),
        color = colorSet.container,
        border = BorderStroke(1.dp, colorSet.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorSet.main,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colorSet.onContainer,
            )
        }
    }
}

/**
 * Qualitative confidence badge reflecting evidential backing strength.
 * Styled as a sleek cyber telemetry indicator.
 */
@Composable
fun ConfidenceBadge(
    level: ConfidenceLevel,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val (label: String, bg: Color, fg: Color, border: Color) = when (level) {
        ConfidenceLevel.HIGH -> {
            if (isDark) {
                Quadruple("High Confidence", Color(0xFF082618), Color(0xFFB9F6CA), NeonEmerald.copy(alpha = 0.5f))
            } else {
                Quadruple("High Confidence", Color(0xFFE0F2F1), Color(0xFF004D40), Color(0xFF80CBC4))
            }
        }
        ConfidenceLevel.MEDIUM -> {
            if (isDark) {
                Quadruple("Moderate Confidence", Color(0xFF332500), Color(0xFFFFF9C4), NeonAmber.copy(alpha = 0.5f))
            } else {
                Quadruple("Moderate Confidence", Color(0xFFFFF3E0), Color(0xFF782500), Color(0xFFFFCC80))
            }
        }
        ConfidenceLevel.LOW -> {
            if (isDark) {
                Quadruple("Low Confidence", Color(0xFF131D31), Color(0xFFB5C4DA), Color(0xFF425675))
            } else {
                Quadruple("Low Confidence", Color(0xFFF1F5F9), Color(0xFF334155), Color(0xFFCBD5E1))
            }
        }
        ConfidenceLevel.UNCERTAIN -> {
            if (isDark) {
                Quadruple("Uncertain Confidence", Color(0xFF092036), Color(0xFFB3E5FC), LaserBlue.copy(alpha = 0.4f))
            } else {
                Quadruple("Uncertain Confidence", Color(0xFFF8FAFC), Color(0xFF475569), Color(0xFFE2E8F0))
            }
        }
    }

    Surface(
        modifier = modifier.semantics {
            contentDescription = "Confidence Level: $label"
        },
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
