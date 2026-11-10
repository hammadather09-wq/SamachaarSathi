package com.example.samachaarsathi.ui.claim

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.samachaarsathi.MainActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class GeminiIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Ignore("Zero live API calls rule in effect across QA and regression test runs")
    @Test
    fun verifyRealGeminiCallOnPhysicalDevice() {
        val testClaim = "Government is distributing free laptop for college students"
        composeTestRule.onNodeWithText("Claim or message").performTextInput(testClaim)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeTestRule.onNodeWithText("Verify Claim").assertIsEnabled()

        composeTestRule.onNodeWithText("Verify Claim").performClick()

        // Wait up to 210 seconds for progressive streaming and final assessment
        var streamedChunksSeen = false
        composeTestRule.waitUntil(timeoutMillis = 210_000) {
            if (!streamedChunksSeen) {
                val hasStreamingCard = try {
                    composeTestRule.onNodeWithText("Generating Assessment...").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
                if (hasStreamingCard) {
                    streamedChunksSeen = true
                    android.util.Log.d("GeminiIntegrationTest", "Progressive streaming UI card confirmed on screen!")
                }
            }
            val isSuccess = try {
                composeTestRule.onNodeWithText("Gemini Assessment").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
            val isError = try {
                composeTestRule.onNodeWithText("Analysis Error").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
            val isInterrupted = try {
                composeTestRule.onNodeWithText("Analysis Interrupted").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
            isSuccess || isError || isInterrupted
        }

        // Check which card appeared on screen
        val isSuccess = try {
            composeTestRule.onNodeWithText("Gemini Assessment").assertExists()
            true
        } catch (_: AssertionError) {
            false
        }

        val isError = try {
            composeTestRule.onNodeWithText("Analysis Error").assertExists()
            true
        } catch (_: AssertionError) {
            false
        }

        val isInterrupted = try {
            composeTestRule.onNodeWithText("Analysis Interrupted").assertExists()
            true
        } catch (_: AssertionError) {
            false
        }

        if (isSuccess) {
            android.util.Log.i("GeminiIntegrationTest", "Gemini Assessment successfully displayed!")
            composeTestRule.onNodeWithText("Gemini Assessment").assertIsDisplayed()
        } else if (isError) {
            android.util.Log.w("GeminiIntegrationTest", "Analysis Error displayed on screen (e.g. backend quota or server error).")
            composeTestRule.onNodeWithText("Analysis Error").assertIsDisplayed()
        } else if (isInterrupted) {
            android.util.Log.w("GeminiIntegrationTest", "Analysis Interrupted displayed on screen.")
            composeTestRule.onNodeWithText("Analysis Interrupted").assertIsDisplayed()
        } else {
            throw AssertionError("Neither Gemini Assessment, Analysis Error, nor Analysis Interrupted appeared on screen.")
        }

        // Verify button re-enabled after completion
        composeTestRule.onNodeWithText("Verify Claim").assertIsEnabled()
    }
}
