package com.example.samachaarsathi.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme

@Composable
private fun DesignSystemCatalogContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(
            title = "Design System: Buttons",
            subtitle = "Primary, loading, disabled, and secondary variations",
            badgeText = "M3",
        )

        PrimaryButton(
            text = "Verify Claim",
            onClick = {},
        )

        PrimaryButton(
            text = "Analyzing Claim...",
            onClick = {},
            isLoading = true,
            loadingText = "Analyzing with Evidence...",
        )

        PrimaryButton(
            text = "Disabled Button",
            onClick = {},
            enabled = false,
        )

        SecondaryButton(
            text = "Select Image / Screenshot",
            onClick = {},
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = "Verdict Badges",
            subtitle = "All 4 standard fact-checking categories",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VerdictBadge(category = VerdictCategory.SUPPORTED)
            VerdictBadge(category = VerdictCategory.CONTRADICTED)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VerdictBadge(category = VerdictCategory.MISLEADING_OR_MISSING_CONTEXT)
            VerdictBadge(category = VerdictCategory.UNVERIFIED)
        }

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = "Confidence Indicators",
            subtitle = "Transparent qualitative confidence levels",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ConfidenceBadge(level = ConfidenceLevel.HIGH)
            ConfidenceBadge(level = ConfidenceLevel.MEDIUM)
            ConfidenceBadge(level = ConfidenceLevel.LOW)
            ConfidenceBadge(level = ConfidenceLevel.UNCERTAIN)
        }

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = "Verification Step Progress",
            subtitle = "Honest pipeline progress tracking",
        )

        StepProgressIndicator(
            steps = listOf(
                VerificationPipelineStep(
                    id = "1",
                    title = "Understanding the claim",
                    state = StepExecutionState.COMPLETED,
                    detail = "Identified 2 verifiable assertions",
                ),
                VerificationPipelineStep(
                    id = "2",
                    title = "Retrieving web evidence",
                    state = StepExecutionState.IN_PROGRESS,
                    detail = "Cross-referencing 3 official portals...",
                ),
                VerificationPipelineStep(
                    id = "3",
                    title = "Comparing evidence & stances",
                    state = StepExecutionState.PENDING,
                ),
                VerificationPipelineStep(
                    id = "4",
                    title = "Synthesizing transparent verdict",
                    state = StepExecutionState.PENDING,
                ),
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = "Feedback Cards",
            subtitle = "Empty and error state feedback",
        )

        EmptyStateCard(
            heading = "No Saved Claims",
            description = "Claims and verdicts you bookmark will appear here for quick reference.",
            actionLabel = "Start New Check",
            onActionClicked = {},
        )

        ErrorStateCard(
            title = "Retrieval Timeout",
            message = "Unable to connect to source index. Check your local connection.",
            onRetry = {},
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = "Two-Tab Navigation",
            subtitle = "Check and Library destinations",
        )

        SamachaarBottomNavigationBar(
            currentDestination = AppDestination.CHECK,
            onNavigateToDestination = {},
        )
    }
}

@Preview(name = "Design System Catalog - Light", showBackground = true)
@Composable
fun DesignSystemCatalogLightPreview() {
    SamachaarSathiTheme(darkTheme = false) {
        Surface {
            DesignSystemCatalogContent()
        }
    }
}

@Preview(name = "Design System Catalog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DesignSystemCatalogDarkPreview() {
    SamachaarSathiTheme(darkTheme = true) {
        Surface {
            DesignSystemCatalogContent()
        }
    }
}
