package com.example.samachaarsathi.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.samachaarsathi.data.GeminiRepository
import com.example.samachaarsathi.ui.claim.ClaimInputViewModel
import com.example.samachaarsathi.ui.theme.SamachaarSathiTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test

class MainAppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    class FakeGeminiRepository : GeminiRepository {
        override fun analyzeClaimStream(claim: String): Flow<String> = flow {
            emit("Analysis")
        }
        override suspend fun analyzeClaim(claim: String): Result<String> = Result.success("Analysis")
    }

    @Test
    fun bottomNavigationBar_rendersBothTabs() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                MainAppScreen()
            }
        }

        composeTestRule.onNodeWithText("Check").assertIsDisplayed()
        composeTestRule.onNodeWithText("Library").assertIsDisplayed()
    }

    @Test
    fun navigateToLibrary_showsLibraryDestinationAndEmptyState() {
        composeTestRule.setContent {
            SamachaarSathiTheme {
                MainAppScreen()
            }
        }

        // Tap Library tab in bottom navigation
        composeTestRule.onNodeWithText("Library").performClick()
        composeTestRule.waitForIdle()

        // Verify Library screen content
        composeTestRule.onNodeWithText("Verification history & saved checks").assertIsDisplayed()
        composeTestRule.onNodeWithText("History (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Checks Yet").assertIsDisplayed()
    }

    @Test
    fun tabSwitching_preservesClaimInputState() {
        val viewModel = ClaimInputViewModel(geminiRepository = FakeGeminiRepository())

        composeTestRule.setContent {
            SamachaarSathiTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }

        val testClaim = "New education policy updates announced"
        composeTestRule.onNodeWithText("Claim or message").performTextInput(testClaim)

        // Switch to Library tab
        composeTestRule.onNodeWithText("Library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Verification history & saved checks").assertIsDisplayed()

        // Switch back to Check tab
        composeTestRule.onNodeWithText("Check").performClick()
        composeTestRule.waitForIdle()

        // Verify input text is fully preserved
        composeTestRule.onNodeWithText(testClaim).assertIsDisplayed()
    }
}
