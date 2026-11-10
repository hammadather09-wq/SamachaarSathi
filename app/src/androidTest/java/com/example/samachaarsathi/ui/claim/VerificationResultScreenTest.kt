package com.example.samachaarsathi.ui.claim

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationVerdict
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import org.junit.Rule
import org.junit.Test

class VerificationResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSource = EvidenceSource(
        id = "src_1",
        title = "PIB Fact Check Press Release on Free Laptop Scheme",
        url = "https://pib.gov.in/factcheck/free-laptops",
        domain = "pib.gov.in",
        snippet = "PIB Fact Check confirmed no free laptop scheme was approved by the ministry.",
        sourceType = SourceType.OFFICIAL_PORTAL,
    )

    private val sampleUnderstanding = ClaimUnderstanding(
        originalClaim = "Government is distributing free laptops to all college students",
        coreClaim = "Government free laptop distribution scheme",
        claimType = "government announcement",
        entities = listOf("Ministry of Education", "PIB"),
        keyAssertions = listOf("Government distributing free laptops to college students"),
    )

    private val sampleEvidenceCollection = EvidenceCollection(
        originalClaim = "Government is distributing free laptops to all college students",
        structuredClaim = sampleUnderstanding,
        assertionEvidence = listOf(
            AssertionEvidence(
                assertionId = "1",
                assertionText = "Government distributing free laptops to college students",
                sources = listOf(sampleSource),
                retrievalStatus = RetrievalStatus.SUCCESS,
            ),
        ),
        overallRetrievalStatus = RetrievalStatus.SUCCESS,
    )

    private val sampleVerdict = VerificationVerdict(
        category = VerdictCategory.CONTRADICTED,
        headline = "Government has NOT authorized any free laptop distribution scheme",
        reasoning = VerdictReasoning(
            primaryExplanation = "Official releases from PIB confirm that no such scheme was approved.",
            keyEvidencePoints = listOf(
                "Ministry of Education issued explicit denial on official website",
            ),
            missingContextNotes = listOf(
                "Viral link directs to an unverified third-party phishing portal",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by official gazette denial and PIB fact-check releases.",
        ),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidenceCollection,
        ),
    )

    @Test
    fun verificationResultScreen_displaysVerdictHeadlineAndConfidence() {
        val uiState = ClaimInputUiState(
            claimText = "Government is distributing free laptops to all college students",
            lastSubmittedClaim = "Government is distributing free laptops to all college students",
            verdictStatus = VerdictStatus.Success(sampleVerdict),
            evidenceStatus = EvidenceStatus.Success(sampleEvidenceCollection),
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                VerificationResultScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                )
            }
        }

        // Screen title
        composeTestRule.onNodeWithText("Verification Assessment").assertIsDisplayed()

        // Analyzed claim
        composeTestRule.onNodeWithText("Government is distributing free laptops to all college students").assertIsDisplayed()

        // Verdict Badge & Headline
        composeTestRule.onNodeWithText("VERIFIED FALSE").assertIsDisplayed()
        composeTestRule.onNodeWithText("High Confidence").assertIsDisplayed()
        composeTestRule.onNodeWithText("Government has NOT authorized any free laptop distribution scheme").assertIsDisplayed()

        // Primary Explanation
        composeTestRule.onNodeWithText("Official releases from PIB confirm that no such scheme was approved.").assertIsDisplayed()

        // "Why this verdict?" Section
        composeTestRule.onNodeWithText("Why this verdict?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ministry of Education issued explicit denial on official website").assertIsDisplayed()
    }

    @Test
    fun verificationResultScreen_displaysSources_andOpensSourceDetailBottomSheet() {
        val uiState = ClaimInputUiState(
            claimText = "Free laptop scheme",
            verdictStatus = VerdictStatus.Success(sampleVerdict),
            evidenceStatus = EvidenceStatus.Success(sampleEvidenceCollection),
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                VerificationResultScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                )
            }
        }

        // Source card is displayed (scroll down to make it visible)
        composeTestRule.onNodeWithText("pib.gov.in").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("PIB Fact Check Press Release on Free Laptop Scheme").assertIsDisplayed()

        // Tap inspect button to open bottom sheet
        composeTestRule.onNodeWithText("Inspect Source Details").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Bottom sheet content verified
        composeTestRule.onNodeWithText("Grounded Content Excerpt").assertIsDisplayed()
        composeTestRule.onNodeWithText("PIB Fact Check confirmed no free laptop scheme was approved by the ministry.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Original Source").assertIsDisplayed()
    }

    @Test
    fun verificationResultScreen_backButton_triggersCallback() {
        var backClicked = false
        val uiState = ClaimInputUiState(
            claimText = "Test Claim",
            verdictStatus = VerdictStatus.Success(sampleVerdict),
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                VerificationResultScreen(
                    uiState = uiState,
                    onNavigateBack = { backClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back to Check Input").performClick()
        assert(backClicked)
    }
}
