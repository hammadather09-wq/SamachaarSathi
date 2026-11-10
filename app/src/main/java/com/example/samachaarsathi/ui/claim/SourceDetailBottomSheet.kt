package com.example.samachaarsathi.ui.claim

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.SourceAssessment
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.ui.component.FuturisticCard
import com.example.samachaarsathi.ui.theme.CyberCardBorder
import com.example.samachaarsathi.ui.theme.CyberDarkSurface
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.LaserBlue
import com.example.samachaarsathi.ui.theme.NeonEmerald

/**
 * Reusable source detail bottom sheet providing complete source provenance,
 * grounded excerpt, institutional tier, and verified URL link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceDetailBottomSheet(
    source: EvidenceSource,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    stanceText: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    val isDark = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = if (isDark) CyberDarkSurface else MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = ElectricCyan.copy(alpha = 0.5f),
                shape = RoundedCornerShape(2.dp),
            ) {
                Spacer(modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // 1. Header: Source Domain & Reliability Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = source.domain ?: "Web Source",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                val tier = SourceAssessment.inferReliabilityTier(source)
                val tierStyle = when (tier) {
                    SourceReliabilityTier.OFFICIAL_RECORD -> SourceTierBadgeStyle(
                        "Tier 1: Official Gazette / Portal",
                        if (isDark) Color(0xFF082618) else Color(0xFFE0F2F1),
                        NeonEmerald.copy(alpha = 0.5f),
                        if (isDark) NeonEmerald else Color(0xFF004D40),
                    )
                    SourceReliabilityTier.INDEPENDENT_FACT_CHECKER -> SourceTierBadgeStyle(
                        "Tier 2: Fact-Check Organization",
                        if (isDark) Color(0xFF092036) else Color(0xFFE1F5FE),
                        LaserBlue.copy(alpha = 0.5f),
                        if (isDark) LaserBlue else Color(0xFF01579B),
                    )
                    SourceReliabilityTier.CREDIBLE_PRESS -> SourceTierBadgeStyle(
                        "Tier 3: Mainstream Press",
                        if (isDark) Color(0xFF142036) else Color(0xFFE8EAF6),
                        ElectricCyan.copy(alpha = 0.4f),
                        if (isDark) ElectricCyan else Color(0xFF1A237E),
                    )
                    SourceReliabilityTier.GENERAL_WEB -> SourceTierBadgeStyle(
                        "Tier 4: General Web",
                        if (isDark) Color(0xFF131D31) else Color(0xFFF5F5F5),
                        CyberCardBorder,
                        if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF424242),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tierStyle.bg,
                    border = BorderStroke(1.dp, tierStyle.border),
                ) {
                    Text(
                        text = tierStyle.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = tierStyle.fg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Source Title / Headline
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Stance to claim (if provided)
            if (stanceText != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricCyan.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.35f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Evidentiary Relationship: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                        )
                        Text(
                            text = stanceText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Grounded Excerpt Section
            Text(
                text = "Grounded Content Excerpt",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
            )
            Spacer(modifier = Modifier.height(8.dp))

            FuturisticCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isDark = isDark,
            ) {
                Text(
                    text = source.snippet?.takeIf { it.isNotBlank() }
                        ?: "No specific text excerpt was returned in the search grounding chunk. Refer to the original publication URL below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp),
                    lineHeight = 22.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Source Metadata Details
            Text(
                text = "Metadata & Provenance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetadataRow(
                    label = "Publication Date",
                    value = source.publishedDate ?: "Date not specified in source metadata",
                )
                MetadataRow(
                    label = "Categorization",
                    value = when (source.sourceType) {
                        SourceType.OFFICIAL_PORTAL -> "Government / Official Gazette"
                        SourceType.FACT_CHECK_ORGANIZATION -> "Independent Fact-Check Newsroom"
                        SourceType.NEWS_MEDIA -> "Accredited News Media"
                        SourceType.WEB_SEARCH, SourceType.OTHER -> "General Web Source"
                    },
                )
                MetadataRow(
                    label = "Source URL",
                    value = source.url,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Action Button: Open Original Source
            Button(
                onClick = { uriHandler.openUri(source.url) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "Open Original Source",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
        )
    }
}

private data class SourceTierBadgeStyle(
    val label: String,
    val bg: Color,
    val border: Color,
    val fg: Color,
)
