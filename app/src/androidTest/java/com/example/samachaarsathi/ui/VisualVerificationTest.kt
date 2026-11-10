package com.example.samachaarsathi.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.example.samachaarsathi.data.model.AssertionAnalysis
import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationVerdict
import com.example.samachaarsathi.ui.claim.ClaimInputUiState
import com.example.samachaarsathi.ui.claim.EvidenceStatus
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import com.example.samachaarsathi.ui.claim.SourceDetailBottomSheet
import com.example.samachaarsathi.ui.claim.UnderstandingStatus
import com.example.samachaarsathi.ui.claim.VerdictStatus
import com.example.samachaarsathi.ui.claim.VerificationResultScreen
import com.example.samachaarsathi.ui.library.LibraryScreen
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import org.junit.Rule
import org.junit.Test

class VisualVerificationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSource = EvidenceSource(
        id = "src_1",
        title = "PIB Fact Check: Clarification on Laptop Distribution Claim",
        url = "https://pib.gov.in/factcheck/laptop-scheme-clarification",
        domain = "pib.gov.in",
        snippet = "PIB Fact Check confirmed no free laptop scheme was approved by the ministry. The viral message links to a fraudulent phishing portal.",
        sourceType = SourceType.OFFICIAL_PORTAL,
        publishedDate = "Nov 4, 2026",
    )

    private val sampleUnderstanding = ClaimUnderstanding(
        originalClaim = "Government announced free laptop scheme for college students",
        coreClaim = "Government free laptop distribution scheme",
        claimType = "government announcement",
        entities = listOf("Ministry of Education", "PIB"),
        keyAssertions = listOf("Government distributing free laptops to college students"),
    )

    private val sampleEvidence = EvidenceCollection(
        originalClaim = "Government announced free laptop scheme for college students",
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
        headline = "Government has NOT authorized any free laptop scheme",
        reasoning = VerdictReasoning(
            primaryExplanation = "Official releases from the Ministry of Education clarify that this viral claim is fraudulent.",
            keyEvidencePoints = listOf(
                "Official PIB Gazette denies the existence of this scheme",
                "Viral link redirects to an unverified phishing domain",
            ),
            missingContextNotes = listOf(
                "No funds have been sanctioned by the central or state governments for this purpose",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by official government portal and accredited fact-checking records.",
        ),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "1",
                    assertionText = "Government distributing free laptops to college students",
                    stance = EvidenceStance.CONTRADICTING,
                    summary = "Contradicted by PIB official gazette denial.",
                ),
            ),
        ),
    )

    @Test
    fun renderVerificationResultsScreen() {
        val uiState = ClaimInputUiState(
            claimText = "Government announced free laptop scheme for college students",
            lastSubmittedClaim = "Government announced free laptop scheme for college students",
            understandingStatus = UnderstandingStatus.Success(sampleUnderstanding),
            evidenceStatus = EvidenceStatus.Success(sampleEvidence),
            verdictStatus = VerdictStatus.Success(sampleVerdict),
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                VerificationResultScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun renderSourceDetailBottomSheet() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                SourceDetailBottomSheet(
                    source = sampleSource,
                    onDismissRequest = {},
                    stanceText = "Contradicts Claim Assertion",
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun renderLibrarySavedScreen() {
        val savedItems = listOf(
            SessionVerificationItem(
                id = "item_1",
                claimText = "Government announced free laptop scheme for college students",
                verdict = sampleVerdict,
                isSaved = true,
                timestampMs = 1730005000000L,
            ),
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = savedItems,
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}
