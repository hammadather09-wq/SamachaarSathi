package com.example.samachaarsathi.ui.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationVerdict
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LibraryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleUnderstanding = ClaimUnderstanding(
        originalClaim = "Claim A",
        coreClaim = "Claim A",
        claimType = "policy",
    )

    private val sampleEvidence = EvidenceCollection(
        originalClaim = "Claim A",
        structuredClaim = sampleUnderstanding,
    )

    private val verdictTrue = VerificationVerdict(
        category = VerdictCategory.SUPPORTED,
        headline = "Official announcement confirmed by gazette",
        reasoning = VerdictReasoning(primaryExplanation = "Confirmed true"),
        confidence = ConfidenceAssessment(level = ConfidenceLevel.HIGH, rationale = "High certainty"),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
        ),
    )

    private val verdictFalse = VerificationVerdict(
        category = VerdictCategory.CONTRADICTED,
        headline = "Scheme is completely fabricated",
        reasoning = VerdictReasoning(primaryExplanation = "Confirmed false"),
        confidence = ConfidenceAssessment(level = ConfidenceLevel.HIGH, rationale = "High certainty"),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
        ),
    )

    private val testItems = listOf(
        SessionVerificationItem(
            id = "item_1",
            claimText = "New metro corridor approved for city",
            verdict = verdictTrue,
            isSaved = false,
            timestampMs = 1730000000000L,
        ),
        SessionVerificationItem(
            id = "item_2",
            claimText = "Free recharge scam message circulating",
            verdict = verdictFalse,
            isSaved = true,
            timestampMs = 1730005000000L,
        ),
    )

    @Test
    fun libraryScreen_rendersHistoryTabWithItemsAndSessionNotice() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = testItems,
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }

        // Header and tab counts
        composeTestRule.onNodeWithText("Library").assertIsDisplayed()
        composeTestRule.onNodeWithText("History (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved (1)").assertIsDisplayed()

        // Items in history
        composeTestRule.onNodeWithText("New metro corridor approved for city").assertIsDisplayed()
        composeTestRule.onNodeWithText("VERIFIED TRUE").assertIsDisplayed()

        composeTestRule.onNodeWithText("Free recharge scam message circulating").assertIsDisplayed()
        composeTestRule.onNodeWithText("VERIFIED FALSE").assertIsDisplayed()

        // Session storage notice
        composeTestRule.onNodeWithText(
            "Session Storage: History and saved items are held in memory for this active session and reset on app restart.",
        ).assertIsDisplayed()
    }

    @Test
    fun libraryScreen_savedTab_showsOnlyBookmarkedItems() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = testItems,
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }

        // Tap Saved tab
        composeTestRule.onNodeWithText("Saved (1)").performClick()
        composeTestRule.waitForIdle()

        // Only saved item is displayed with badge
        composeTestRule.onNodeWithText("Free recharge scam message circulating").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Check").assertIsDisplayed()

        // Unsaved item is not displayed
        composeTestRule.onNodeWithText("New metro corridor approved for city").assertDoesNotExist()
    }

    @Test
    fun libraryScreen_searchFilter_filtersItems() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = testItems,
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }

        // Type search query
        composeTestRule.onNodeWithText("Search checks by claim or finding...").performTextInput("metro")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New metro corridor approved for city").assertIsDisplayed()
        composeTestRule.onNodeWithText("Free recharge scam message circulating").assertDoesNotExist()
    }

    @Test
    fun libraryScreen_categoryFilter_filtersByVerdict() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = testItems,
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }

        // Tap "Verified False" category filter chip
        composeTestRule.onNodeWithText("Verified False").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Free recharge scam message circulating").assertIsDisplayed()
        composeTestRule.onNodeWithText("New metro corridor approved for city").assertDoesNotExist()
    }

    @Test
    fun libraryScreen_tappingItem_invokesCallback() {
        var selectedItem: SessionVerificationItem? = null

        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = testItems,
                    onSelectHistoryItem = { selectedItem = it },
                    onToggleSave = {},
                    onNavigateToCheck = {},
                )
            }
        }

        composeTestRule.onNodeWithText("New metro corridor approved for city").performClick()
        assertEquals("item_1", selectedItem?.id)
    }

    @Test
    fun libraryScreen_emptyState_showsAppropriateAction() {
        var navigatedToCheck = false

        composeTestRule.setContent {
            SamachaarSathiTheme {
                LibraryScreen(
                    sessionHistory = emptyList(),
                    onSelectHistoryItem = {},
                    onToggleSave = {},
                    onNavigateToCheck = { navigatedToCheck = true },
                )
            }
        }

        composeTestRule.onNodeWithText("No Checks Yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verify a Claim").performClick()
        assertTrue(navigatedToCheck)
    }
}
