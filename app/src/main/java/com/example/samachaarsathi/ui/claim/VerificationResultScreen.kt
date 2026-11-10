package com.example.samachaarsathi.ui.claim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samachaarsathi.data.model.AssertionAnalysis
import com.example.samachaarsathi.data.model.DemoFixtures
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.SourceAssessment
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerificationVerdict
import com.example.samachaarsathi.ui.component.ConfidenceBadge
import com.example.samachaarsathi.ui.component.EmptyStateCard
import com.example.samachaarsathi.ui.component.ErrorStateCard
import com.example.samachaarsathi.ui.component.FuturisticCard
import com.example.samachaarsathi.ui.component.MarkdownText
import com.example.samachaarsathi.ui.component.SecondaryButton
import com.example.samachaarsathi.ui.component.SectionHeader
import com.example.samachaarsathi.ui.component.StepExecutionState
import com.example.samachaarsathi.ui.component.StepProgressIndicator
import com.example.samachaarsathi.ui.component.VerdictBadge
import com.example.samachaarsathi.ui.component.VerificationPipelineStep
import com.example.samachaarsathi.ui.share.ShareHelper
import com.example.samachaarsathi.ui.theme.CyberCardBorder
import com.example.samachaarsathi.ui.theme.CyberDarkSurface
import com.example.samachaarsathi.ui.theme.CyberElevatedCard
import com.example.samachaarsathi.ui.theme.DeepSpaceBlack
import com.example.samachaarsathi.ui.theme.ElectricCyan
import com.example.samachaarsathi.ui.theme.LaserBlue
import com.example.samachaarsathi.ui.theme.NeonAmber
import com.example.samachaarsathi.ui.theme.NeonEmerald
import com.example.samachaarsathi.ui.theme.SamachaarMotion
import com.example.samachaarsathi.ui.theme.VerdictColors

/**
 * Dedicated, premium verification results screen presenting synthesized verdicts,
 * qualitative confidence, structured decomposition, detailed reasoning, and
 * grounded evidence sources with bottom sheet inspection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationResultScreen(
    uiState: ClaimInputUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRetrieveEvidenceClicked: () -> Unit = {},
    initialScrollToSources: Boolean = false,
    initialScrollToShare: Boolean = false,
    initialShowSourceDetails: Boolean = false,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var selectedSourceForDetail by remember {
        mutableStateOf<EvidenceSource?>(if (initialShowSourceDetails) DemoFixtures.sampleSource else null)
    }
    var selectedSourceStance by remember {
        mutableStateOf<String?>(if (initialShowSourceDetails) "Contradicts claim" else null)
    }

    LaunchedEffect(initialScrollToSources, initialScrollToShare) {
        if (initialScrollToShare) {
            scrollState.scrollTo(650)
        } else if (initialScrollToSources) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDark) DeepSpaceBlack else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Verification Assessment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Back to Check Input"
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Check Input",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val claim = uiState.lastSubmittedClaim ?: uiState.claimText
                            val verdict = (uiState.verdictStatus as? VerdictStatus.Success)?.verdict
                            ShareHelper.shareText(context, claim, verdict)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Share Fact-Check Report"
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Fact-Check Report",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isDark) CyberDarkSurface else MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Original Claim Card
            OriginalClaimSummaryCard(
                claimText = uiState.lastSubmittedClaim ?: uiState.claimText,
            )

            // 2. Active Pipeline Progress if loading
            if (uiState.isLoading) {
                VerificationProgressSection(uiState = uiState)
            }

            // 3. Error state presentation if any pipeline step failed
            when {
                uiState.verdictStatus is VerdictStatus.Error -> {
                    ErrorStateCard(
                        title = "Verdict Synthesis Error",
                        message = uiState.verdictStatus.message,
                        onRetry = onNavigateBack,
                        retryLabel = "Back to Claim Input",
                    )
                }
                uiState.evidenceStatus is EvidenceStatus.Error -> {
                    ErrorStateCard(
                        title = "Evidence Retrieval Error",
                        message = uiState.evidenceStatus.message,
                        onRetry = onRetrieveEvidenceClicked,
                        retryLabel = "Retry Evidence Retrieval",
                    )
                }
                uiState.understandingStatus is UnderstandingStatus.Error -> {
                    ErrorStateCard(
                        title = "Claim Understanding Error",
                        message = uiState.understandingStatus.message,
                        onRetry = onNavigateBack,
                        retryLabel = "Back to Claim Input",
                    )
                }
                uiState.analysisStatus is AnalysisStatus.Error -> {
                    ErrorStateCard(
                        title = "Analysis Error",
                        message = uiState.analysisStatus.message,
                        onRetry = onNavigateBack,
                        retryLabel = "Back to Claim Input",
                    )
                }
            }

            // 4. Primary Verdict Card (if verdict exists)
            val verdict = (uiState.verdictStatus as? VerdictStatus.Success)?.verdict
            if (verdict != null) {
                PrimaryVerdictCard(verdict = verdict)

                // 4b. Native Sharing Affordance
                ShareOptionsCard(
                    onShareText = {
                        val claim = uiState.lastSubmittedClaim ?: uiState.claimText
                        ShareHelper.shareText(context, claim, verdict)
                    },
                    onShareImageCard = {
                        val claim = uiState.lastSubmittedClaim ?: uiState.claimText
                        ShareHelper.shareImageCard(context, claim, verdict)
                    },
                )

                // 5. Expandable "Why this verdict?" Reasoning Section
                WhyThisVerdictCard(verdict = verdict)
            } else if (uiState.analysisStatus is AnalysisStatus.Success && !uiState.isLoading) {
                // Fallback for single-prompt Brick 2 response if available
                DirectGeminiResponseCard(response = uiState.analysisStatus.response)
            }

            // 6. Claim Understanding Decomposition Card (if extracted)
            val understanding = (uiState.understandingStatus as? UnderstandingStatus.Success)?.understanding
            if (understanding != null) {
                SectionHeader(
                    title = "Structured Claim Decomposition",
                    subtitle = "Entity, timeline, and assertion analysis extracted by on-device engine",
                )
                ClaimUnderstandingCard(understanding = understanding)
            }

            // 7. Grounded Evidence Sources Section
            val evidenceCollection = (uiState.evidenceStatus as? EvidenceStatus.Success)?.evidence
            if (evidenceCollection != null) {
                EvidenceSourcesSection(
                    collection = evidenceCollection,
                    onInspectSource = { source, stance ->
                        selectedSourceForDetail = source
                        selectedSourceStance = stance
                    },
                )
            } else if (uiState.evidenceStatus is EvidenceStatus.NotRetrieved && !uiState.isLoading) {
                EvidenceNotYetRetrievedSection(onRetrieveClicked = onRetrieveEvidenceClicked)
            }

            // 8. Bottom Navigation Action
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = "Back to Claim Input",
                onClick = onNavigateBack,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Source detail inspection bottom sheet
    selectedSourceForDetail?.let { source ->
        SourceDetailBottomSheet(
            source = source,
            onDismissRequest = {
                selectedSourceForDetail = null
                selectedSourceStance = null
            },
            stanceText = selectedSourceStance,
        )
    }
}

/**
 * Clean obsidian card summarizing the user-submitted claim with cyber telemetry accent.
 */
@Composable
private fun OriginalClaimSummaryCard(
    claimText: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "ANALYZED CLAIM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ElectricCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = claimText.ifBlank { "No claim text provided" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
            )
        }
    }
}

/**
 * Pipeline progress indicator showing which stage of verification is executing.
 */
@Composable
private fun VerificationProgressSection(
    uiState: ClaimInputUiState,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val steps = listOf(
                VerificationPipelineStep(
                    id = "step_understanding",
                    title = "Structured Understanding",
                    detail = "Extracting factual assertions, entities, and context",
                    state = when (uiState.understandingStatus) {
                        is UnderstandingStatus.Loading -> StepExecutionState.IN_PROGRESS
                        is UnderstandingStatus.Success -> StepExecutionState.COMPLETED
                        is UnderstandingStatus.Error -> StepExecutionState.FAILED
                        UnderstandingStatus.Idle -> StepExecutionState.PENDING
                    },
                ),
                VerificationPipelineStep(
                    id = "step_evidence",
                    title = "Evidence Grounding",
                    detail = "Searching verified web sources via Google Search",
                    state = when (uiState.evidenceStatus) {
                        is EvidenceStatus.Loading -> StepExecutionState.IN_PROGRESS
                        is EvidenceStatus.Success -> StepExecutionState.COMPLETED
                        is EvidenceStatus.Error -> StepExecutionState.FAILED
                        EvidenceStatus.NotRetrieved -> StepExecutionState.PENDING
                    },
                ),
                VerificationPipelineStep(
                    id = "step_verdict",
                    title = "Verdict Synthesis",
                    detail = "Evaluating corroboration and generating definitive report",
                    state = when (uiState.verdictStatus) {
                        is VerdictStatus.Loading -> StepExecutionState.IN_PROGRESS
                        is VerdictStatus.Success -> StepExecutionState.COMPLETED
                        is VerdictStatus.Error -> StepExecutionState.FAILED
                        VerdictStatus.Idle -> StepExecutionState.PENDING
                    },
                ),
            )

            Text(
                text = "Verification In Progress",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            StepProgressIndicator(steps = steps)
        }
    }
}

/**
 * Luminous, multi-layered circular holographic verification seal inspired by Cyber Intelligence visual identity.
 * Features concentric glowing laser rings and prominent semantic icon.
 */
@Composable
private fun CyberVerdictSeal(
    category: VerdictCategory,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    size: Dp = 56.dp,
) {
    val colorSet = VerdictColors.forCategory(category, isDark)
    val icon = when (category) {
        VerdictCategory.SUPPORTED -> Icons.Default.Check
        VerdictCategory.CONTRADICTED -> Icons.Default.Close
        VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> Icons.Default.Warning
        VerdictCategory.UNVERIFIED -> Icons.Default.Info
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // Outer aura ring
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = colorSet.main.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, colorSet.main.copy(alpha = 0.35f)),
        ) {}

        // Mid ring
        Surface(
            modifier = Modifier.size(size * 0.78f),
            shape = CircleShape,
            color = colorSet.container,
            border = BorderStroke(1.5.dp, colorSet.main.copy(alpha = 0.8f)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorSet.main,
                    modifier = Modifier.size(size * 0.44f),
                )
            }
        }
    }
}

/**
 * Primary verdict display featuring prominent semantic styling, qualitative confidence,
 * headline assertion, core explanation, and cybernetic holographic seal.
 */
@Composable
private fun PrimaryVerdictCard(
    verdict: VerificationVerdict,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val colorSet = VerdictColors.forCategory(verdict.category, isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    color = if (isDark) colorSet.border else colorSet.border.copy(alpha = 0.7f),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            colorSet.container.copy(alpha = 0.92f),
                            CyberElevatedCard.copy(alpha = 0.98f),
                        )
                    } else {
                        listOf(
                            colorSet.container,
                            MaterialTheme.colorScheme.surface,
                        )
                    },
                )
            )
            .animateContentSize(SamachaarMotion.mediumTween()),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Badges Row: Verdict Badge + Qualitative Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VerdictBadge(
                    category = verdict.category,
                    isDark = isDark,
                )
                ConfidenceBadge(
                    level = verdict.confidence.level,
                    isDark = isDark,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Headline & Floating Cyber Seal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = verdict.headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorSet.onContainer,
                    lineHeight = 28.sp,
                    modifier = Modifier.weight(1f),
                )

                CyberVerdictSeal(
                    category = verdict.category,
                    isDark = isDark,
                    size = 54.dp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Explanation
            Text(
                text = verdict.reasoning.primaryExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = colorSet.onContainer.copy(alpha = 0.95f),
                lineHeight = 23.sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confidence rationale note
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) CyberDarkSurface.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, colorSet.main.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colorSet.main,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Certainty Assessment: ${verdict.confidence.rationale}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorSet.onContainer.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

/**
 * Clean card providing native sharing actions for WhatsApp and other channels.
 */
@Composable
private fun ShareOptionsCard(
    onShareText: () -> Unit,
    onShareImageCard: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val haptic = LocalHapticFeedback.current

    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Share Fact-Check Report",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Help counter misinformation by sharing verified evidence on WhatsApp or social media.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onShareText()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share Text",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onShareImageCard()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Image Card",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * Expandable card breaking down why the verdict was reached,
 * including key evidence points, missing context, and assertion-by-assertion analysis.
 */
@Composable
private fun WhyThisVerdictCard(
    verdict: VerificationVerdict,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    var isExpanded by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = SamachaarMotion.fastTween(),
        label = "ArrowRotation",
    )

    FuturisticCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(SamachaarMotion.mediumTween()),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isExpanded = !isExpanded
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why this verdict?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isExpanded = !isExpanded
                    },
                ) {
                    Text(
                        text = if (isExpanded) "Hide Details" else "Show Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                        tint = ElectricCyan,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Key evidence points if available
                    if (verdict.reasoning.keyEvidencePoints.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Key Evidence Points:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricCyan,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            verdict.reasoning.keyEvidencePoints.forEach { point ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan,
                                    )
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 21.sp,
                                    )
                                }
                            }
                        }
                    }

                    // Missing context notes if available
                    if (verdict.reasoning.missingContextNotes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF2B2004).copy(alpha = 0.75f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.45f)),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Missing Context & Important Nuances",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) NeonAmber else MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                verdict.reasoning.missingContextNotes.forEach { note ->
                                    Text(
                                        text = "• $note",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFFFF9C4) else MaterialTheme.colorScheme.onSecondaryContainer,
                                        lineHeight = 19.sp,
                                    )
                                }
                            }
                        }
                    }

                    // Assertion-by-assertion analysis list
                    val assertions = verdict.evidenceAnalysis.assertionAnalyses
                    if (assertions.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = CyberCardBorder.copy(alpha = 0.6f),
                        )
                        Text(
                            text = "Assertion Corroboration Breakdown (${assertions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        assertions.forEach { assertion ->
                            AssertionAnalysisItem(assertion = assertion)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays a single decomposed assertion with its status and corroboration explanation.
 */
@Composable
private fun AssertionAnalysisItem(
    assertion: AssertionAnalysis,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val assertionCategory = when (assertion.stance) {
        EvidenceStance.SUPPORTING -> VerdictCategory.SUPPORTED
        EvidenceStance.CONTRADICTING -> VerdictCategory.CONTRADICTED
        EvidenceStance.INCONCLUSIVE -> VerdictCategory.MISLEADING_OR_MISSING_CONTEXT
        EvidenceStance.INSUFFICIENT_EVIDENCE -> VerdictCategory.UNVERIFIED
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isDark) CyberDarkSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        border = BorderStroke(1.dp, if (isDark) CyberCardBorder.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Assertion #${assertion.assertionId}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                )
                VerdictBadge(
                    category = assertionCategory,
                    showIcon = false,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
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

/**
 * Section presenting grounded evidence sources with institutional tier badges and click-to-inspect affordances.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvidenceSourcesSection(
    collection: EvidenceCollection,
    onInspectSource: (EvidenceSource, String?) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val sources = collection.allSources

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(
            title = "Grounded Evidence Sources",
            subtitle = "Discovered via Google Search Grounding to evaluate factual assertions",
            badgeText = "${sources.size} Sources",
        )

        // Grounding transparency disclaimer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = if (isDark) CyberDarkSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, if (isDark) CyberCardBorder.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        ) {
            Text(
                text = "Attribution Notice: These sources were gathered to evaluate claims against public reporting. Discovery does not constitute an endorsement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp),
            )
        }

        if (sources.isEmpty()) {
            EmptyStateCard(
                heading = "No External Sources Found",
                description = "No corroborated public reporting was retrieved for this specific claim.",
            )
        } else {
            sources.forEach { source ->
                EvidenceSourceResultCard(
                    source = source,
                    onInspectClicked = { onInspectSource(source, null) },
                )
            }
        }
    }
}

/**
 * Card for an individual evidence source with domain, reliability tier, excerpt, and inspect button.
 */
@Composable
private fun EvidenceSourceResultCard(
    source: EvidenceSource,
    onInspectClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val tier = SourceAssessment.inferReliabilityTier(source)
    val style = when (tier) {
        SourceReliabilityTier.OFFICIAL_RECORD -> TierBadgeStyle(
            "✓ Official Record",
            if (isDark) Color(0xFF082618) else Color(0xFFE0F2F1),
            NeonEmerald.copy(alpha = 0.5f),
            if (isDark) NeonEmerald else Color(0xFF004D40),
        )
        SourceReliabilityTier.INDEPENDENT_FACT_CHECKER -> TierBadgeStyle(
            "✓ Fact-Check Org",
            if (isDark) Color(0xFF092036) else Color(0xFFE1F5FE),
            LaserBlue.copy(alpha = 0.5f),
            if (isDark) LaserBlue else Color(0xFF01579B),
        )
        SourceReliabilityTier.CREDIBLE_PRESS -> TierBadgeStyle(
            "✓ Mainstream Press",
            if (isDark) Color(0xFF142036) else Color(0xFFE8EAF6),
            ElectricCyan.copy(alpha = 0.4f),
            if (isDark) ElectricCyan else Color(0xFF1A237E),
        )
        SourceReliabilityTier.GENERAL_WEB -> TierBadgeStyle(
            "✓ Grounded Web Source",
            if (isDark) Color(0xFF131D31) else Color(0xFFF5F5F5),
            CyberCardBorder,
            if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF424242),
        )
    }

    FuturisticCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onInspectClicked),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Domain + Luminous Tier Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = style.bg,
                    border = BorderStroke(1.dp, style.border),
                ) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = style.fg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = source.domain ?: "Web Source",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
            )

            // Snippet
            if (!source.snippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${source.snippet}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    lineHeight = 19.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inspect affordance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = onInspectClicked,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "Inspect Source Details",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan,
                    )
                }
            }
        }
    }
}

/**
 * Card shown when evidence grounding has not yet been triggered.
 */
@Composable
private fun EvidenceNotYetRetrievedSection(
    onRetrieveClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "External Evidence Not Yet Retrieved",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Search Google Grounding to cross-reference identified assertions against accredited sources.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRetrieveClicked,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
            ) {
                Text(
                    text = "Search Grounded Sources",
                    color = ElectricCyan,
                )
            }
        }
    }
}

/**
 * Display for single-prompt Gemini responses (fallback compatibility with Brick 2).
 */
@Composable
private fun DirectGeminiResponseCard(
    response: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    FuturisticCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isDark = isDark,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gemini Analysis Assessment",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
            )
            Spacer(modifier = Modifier.height(8.dp))
            MarkdownText(markdown = response)
        }
    }
}

private data class TierBadgeStyle(
    val label: String,
    val bg: Color,
    val border: Color,
    val fg: Color,
)
