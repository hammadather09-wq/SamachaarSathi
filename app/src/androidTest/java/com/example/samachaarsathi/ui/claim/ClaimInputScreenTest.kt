package com.example.samachaarsathi.ui.claim

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.samachaarsathi.data.ClaimUnderstandingRepository
import com.example.samachaarsathi.data.EvidenceRetrievalRepository
import com.example.samachaarsathi.data.GeminiRepository
import com.example.samachaarsathi.data.image.OcrExtractionResult
import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClaimInputScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    class FakeGeminiRepository : GeminiRepository {
        var lastAnalyzedClaim: String? = null

        override fun analyzeClaimStream(claim: String): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
            lastAnalyzedClaim = claim
            emit("Preliminary AI assessment")
        }

        override suspend fun analyzeClaim(claim: String): Result<String> {
            lastAnalyzedClaim = claim
            return Result.success("Preliminary AI assessment")
        }
    }

    class FakeClaimUnderstandingRepository : ClaimUnderstandingRepository {
        override suspend fun understandClaim(claim: String): Result<ClaimUnderstanding> {
            val understanding = ClaimUnderstanding(
                originalClaim = claim,
                coreClaim = "Decomposed core claim for testing",
                claimType = "government announcement",
                entities = listOf("State Government", "Education Ministry"),
                keyAssertions = listOf("Government announced free laptop scheme"),
                ambiguityNotes = emptyList(),
            )
            return Result.success(understanding)
        }
    }

    class FakeEvidenceRetrievalRepository : EvidenceRetrievalRepository {
        override suspend fun retrieveEvidenceForUnderstanding(
            understanding: ClaimUnderstanding
        ): Result<EvidenceCollection> = Result.success(
            EvidenceCollection(
                originalClaim = understanding.originalClaim,
                structuredClaim = understanding,
                assertionEvidence = listOf(
                    AssertionEvidence(
                        assertionId = "assertion_1",
                        assertionText = understanding.coreClaim,
                        sources = listOf(
                            EvidenceSource(
                                id = "src_pib",
                                title = "PIB Official Clarification",
                                url = "https://pib.gov.in/press/12345",
                                domain = "pib.gov.in",
                                snippet = "Confirmed scheme is fraudulent.",
                                sourceType = SourceType.OFFICIAL_PORTAL,
                                relatedAssertionIds = listOf("assertion_1"),
                            )
                        ),
                        retrievalStatus = RetrievalStatus.SUCCESS,
                    )
                ),
                overallRetrievalStatus = RetrievalStatus.SUCCESS,
            )
        )

        override suspend fun retrieveEvidenceForAssertion(
            assertionId: String,
            assertionText: String
        ): Result<AssertionEvidence> = Result.success(
            AssertionEvidence(
                assertionId = assertionId,
                assertionText = assertionText,
                retrievalStatus = RetrievalStatus.SUCCESS,
            )
        )
    }

    @Test
    fun cyberHeroAndConsoleElements_areDisplayed() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute()
            }
        }

        composeTestRule.onNodeWithText("TRUTH ENGINE • STANDBY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Question everything.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verify with evidence.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Understand claims. Examine sources. Discover context.").assertIsDisplayed()
        composeTestRule.onNodeWithText("CLAIM INPUT CONSOLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evidence-led verification · Transparent results").assertIsDisplayed()
    }

    @Test
    fun verifyButtonDisabled_whenInputEmptyOrWhitespace() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute()
            }
        }

        // Initially empty
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsNotEnabled()

        // Whitespace only
        composeTestRule.onNodeWithText("Paste a message, article link or claim to investigate...").performTextInput("    \n  ")
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsNotEnabled()
    }

    @Test
    fun buttonEnabledAndSubmits_whenNormalClaimEntered() {
        val fakeRepo = FakeGeminiRepository()
        val fakeUnderstandingRepo = FakeClaimUnderstandingRepository()
        val viewModel = ClaimInputViewModel(
            geminiRepository = fakeRepo,
            claimUnderstandingRepository = fakeUnderstandingRepo,
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute(viewModel = viewModel)
            }
        }

        val testClaim = "Breaking news: Moon made of cheese announced by NASA"
        composeTestRule.onNodeWithText("Paste a message, article link or claim to investigate...").performTextInput(testClaim)
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsEnabled()

        composeTestRule.onNodeWithText("VERIFY CLAIM").performClick()
        composeTestRule.waitForIdle()

        assertEquals(testClaim, fakeRepo.lastAnalyzedClaim)
        assertEquals(testClaim, viewModel.uiState.value.lastSubmittedClaim)
    }

    @Test
    fun multilinePastedText_enablesButtonAndCapturesTrimmedClaim() {
        val fakeRepo = FakeGeminiRepository()
        val fakeUnderstandingRepo = FakeClaimUnderstandingRepository()
        val viewModel = ClaimInputViewModel(
            geminiRepository = fakeRepo,
            claimUnderstandingRepository = fakeUnderstandingRepo,
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute(viewModel = viewModel)
            }
        }

        val multilineMessage = "URGENT FORWARD:\nPlease be aware of this scheme.\nCirculate widely!"
        composeTestRule.onNodeWithText("Paste a message, article link or claim to investigate...").performTextInput(multilineMessage)
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsEnabled()

        composeTestRule.onNodeWithText("VERIFY CLAIM").performClick()
        composeTestRule.waitForIdle()

        assertEquals(multilineMessage, fakeRepo.lastAnalyzedClaim)
        assertEquals(multilineMessage, viewModel.uiState.value.lastSubmittedClaim)
    }

    @Test
    fun clearButton_clearsInputAndDisablesVerifyButton() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute()
            }
        }

        composeTestRule.onNodeWithText("Paste a message, article link or claim to investigate...").performTextInput("Sample claim to clear")
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsEnabled()

        // Clear action appears
        composeTestRule.onNodeWithText("Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear").performClick()

        // Button disabled again
        composeTestRule.onNodeWithText("VERIFY CLAIM").assertIsNotEnabled()
    }

    @Test
    fun telemetryState_reflectsHonestEngineState() {
        val fakeRepo = FakeGeminiRepository()
        val fakeUnderstandingRepo = FakeClaimUnderstandingRepository()
        val viewModel = ClaimInputViewModel(
            geminiRepository = fakeRepo,
            claimUnderstandingRepository = fakeUnderstandingRepo,
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute(viewModel = viewModel)
            }
        }

        // Standby initially
        composeTestRule.onNodeWithText("TRUTH ENGINE • STANDBY").assertIsDisplayed()

        // Input active when text entered
        composeTestRule.onNodeWithText("Paste a message, article link or claim to investigate...").performTextInput("Testing claim input")
        composeTestRule.onNodeWithText("CONSOLE • INPUT ACTIVE").assertIsDisplayed()
    }

    @Test
    fun consoleActionIcons_arePresent() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputRoute()
            }
        }

        composeTestRule.onNodeWithContentDescription("Paste from clipboard").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Insert article link").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Attach image or screenshot for OCR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Input supported").assertIsDisplayed()
    }

    @Test
    fun ocrResult_displaysUseExtractedTextButton_andTransfersText() {
        var transferredText = ""
        val ocrResult = OcrExtractionResult(
            rawExtractedText = "Government to offer free tablets to all college finalists.",
            cleanedClaimText = "Government to offer free tablets to all college finalists.",
            forwardingMetadata = null,
            hasReadableText = true,
        )

        composeTestRule.setContent {
            SamachaarSathiTheme {
                ClaimInputScreen(
                    uiState = ClaimInputUiState(
                        ocrResult = ocrResult,
                    ),
                    onClaimTextChanged = {},
                    onVerifyClaimClicked = {},
                    onUseExtractedText = { transferredText = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Image Attached").assertIsDisplayed()
        composeTestRule.onNodeWithText("Use Extracted Text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Use Extracted Text").performClick()

        assertEquals("Government to offer free tablets to all college finalists.", transferredText)
    }
}
