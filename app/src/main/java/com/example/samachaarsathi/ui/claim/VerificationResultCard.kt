package com.example.samachaarsathi.ui.claim

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerificationVerdict

@Composable
fun VerificationResultCard(
    verdictStatus: VerdictStatus,
    modifier: Modifier = Modifier,
) {
    when (verdictStatus) {
        is VerdictStatus.Idle -> Unit
        is VerdictStatus.Loading -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Synthesizing Evidence & Generating Verdict...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cross-referencing assertions, evaluating source stances, and assessing certainty...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        is VerdictStatus.Error -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verification Synthesis Error",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = verdictStatus.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
        is VerdictStatus.Success -> {
            VerdictContentCard(verdict = verdictStatus.verdict, modifier = modifier)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VerdictContentCard(
    verdict: VerificationVerdict,
    modifier: Modifier = Modifier,
) {
    val categoryLabel = when (verdict.category) {
        VerdictCategory.SUPPORTED -> "CONFIRMED SUPPORTED"
        VerdictCategory.CONTRADICTED -> "CONTRADICTED / DEBUNKED"
        VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> "MISLEADING / MISSING CONTEXT"
        VerdictCategory.UNVERIFIED -> "UNVERIFIED"
    }

    val headerColors = when (verdict.category) {
        VerdictCategory.SUPPORTED -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        VerdictCategory.CONTRADICTED -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        VerdictCategory.UNVERIFIED -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = headerColors,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Verdict Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Verification Verdict",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                AssistChip(
                    onClick = {},
                    label = { Text(categoryLabel, fontWeight = FontWeight.Bold) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = verdict.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Explanation
            Text(
                text = verdict.reasoning.primaryExplanation,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Confidence Assessment
            Text(
                text = "Confidence Assessment",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val levelLabel = when (verdict.confidence.level) {
                    ConfidenceLevel.HIGH -> "High Certainty"
                    ConfidenceLevel.MEDIUM -> "Moderate Certainty"
                    ConfidenceLevel.LOW -> "Low Certainty"
                    ConfidenceLevel.UNCERTAIN -> "Uncertain / Unverified"
                }
                AssistChip(onClick = {}, label = { Text(levelLabel) })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = verdict.confidence.rationale,
                style = MaterialTheme.typography.bodySmall,
            )

            // Assertion Breakdown
            if (verdict.evidenceAnalysis.assertionAnalyses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Assertion-by-Assertion Analysis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                verdict.evidenceAnalysis.assertionAnalyses.forEachIndexed { idx, assertion ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Assertion #${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                val stanceLabel = when (assertion.stance) {
                                    EvidenceStance.SUPPORTING -> "Supported"
                                    EvidenceStance.CONTRADICTING -> "Contradicted"
                                    EvidenceStance.INCONCLUSIVE -> "Inconclusive"
                                    EvidenceStance.INSUFFICIENT_EVIDENCE -> "Insufficient Evidence"
                                }
                                AssistChip(onClick = {}, label = { Text(stanceLabel) })
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = assertion.assertionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (assertion.summary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = assertion.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // Missing Context or Gaps
            if (verdict.reasoning.missingContextNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Missing Context / Qualifications",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        verdict.reasoning.missingContextNotes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Evidentiary Limitations
            if (verdict.limitations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Verification Limitations",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        verdict.limitations.forEach { limit ->
                            Text(
                                text = "• ${limit.description} (${limit.impact})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
