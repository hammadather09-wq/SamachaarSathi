package com.example.samachaarsathi.data

import android.util.Log
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ClaimUnderstandingParser
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface for structured claim understanding.
 */
interface ClaimUnderstandingRepository {
    suspend fun understandClaim(claim: String): Result<ClaimUnderstanding>
}

/**
 * Implementation of [ClaimUnderstandingRepository] using Firebase AI Logic with Gemini 3.8 Flash.
 */
class FirebaseClaimUnderstandingRepository(
    private val model: GenerativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = CLAIM_UNDERSTANDING_SCHEMA
            },
        ),
) : ClaimUnderstandingRepository {

    override suspend fun understandClaim(claim: String): Result<ClaimUnderstanding> = withContext(Dispatchers.IO) {
        val trimmedClaim = claim.trim()
        if (trimmedClaim.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Claim cannot be empty or blank"))
        }

        val prompt = buildUnderstandingPrompt(trimmedClaim)
        try {
            Log.d(TAG, "Calling Gemini for structured claim understanding (prompt length: ${prompt.length})...")
            val response = model.generateContent(prompt)
            val responseText = response.text
            if (responseText.isNullOrBlank()) {
                Log.w(TAG, "Gemini returned empty response for claim understanding")
                return@withContext Result.failure(IllegalStateException("No response text returned by model for claim understanding"))
            }

            Log.d(TAG, "Gemini claim understanding response received (${responseText.length} chars)")
            ClaimUnderstandingParser.parse(responseText, trimmedClaim)
        } catch (e: Exception) {
            Log.e(TAG, "Error in understandClaim", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "ClaimUnderstandingRepo"

        val CLAIM_UNDERSTANDING_SCHEMA: Schema = Schema.obj(
            properties = mapOf(
                "coreClaim" to Schema.string("The main factual assertion in clear, concise language"),
                "claimType" to Schema.string("Category: government announcement, public policy, health, finance, science, technology, politics, other"),
                "entities" to Schema.array(Schema.string("Explicit person, organization, department, company, product")),
                "dateReferences" to Schema.array(Schema.string("Explicit date or time period mentioned")),
                "locationReferences" to Schema.array(Schema.string("Explicit place, state, country or geographical reference")),
                "keyAssertions" to Schema.array(Schema.string("Independent factual statement that can be verified")),
                "evidenceRequirements" to Schema.array(Schema.string("Specific primary source, document or official body needed to verify")),
                "ambiguityNotes" to Schema.array(Schema.string("Unspecified, unclear, or missing context details")),
                "language" to Schema.string("Detected language code or name, e.g. en, hi"),
            ),
            optionalProperties = listOf(
                "entities",
                "dateReferences",
                "locationReferences",
                "evidenceRequirements",
                "ambiguityNotes",
                "language",
            ),
        )

        fun buildUnderstandingPrompt(claim: String): String = """
            You are the Claim Understanding Engine of SamachaarSathi, an Indian misinformation combatting application.
            Analyze the user's raw claim and extract a structured representation for subsequent factual verification.

            CRITICAL RULES:
            1. What is the core claim being asserted? Summarize it in clear, concise language.
            2. Identify all individual factual assertions that can be independently checked. If the claim contains multiple assertions (e.g. government announced scheme AND applications open next month), break them into separate keyAssertions.
            3. Identify all explicitly mentioned entities (people, government bodies, organizations, departments, schemes, products). DO NOT invent or hallucinate entities not present.
            4. Extract explicit date or time references (e.g., "next month", "starting tomorrow", "in 2024"). Do not assume dates if not stated.
            5. Extract explicit location or geographical references. Do not assume location (e.g., country or state) if not stated.
            6. Note any ambiguities, missing context, or unspecified details (e.g., "Which state government?", "Which student category?"). Record missing context in ambiguityNotes rather than guessing.
            7. Identify concrete evidence requirements: what primary sources or documents (e.g., official government portal, gazette notification, ministry press release) would be needed to verify each assertion.
            8. Classify the claimType into one of: government announcement, public policy, health, finance, science, technology, politics, or other.
            9. Detect the language of the original claim.

            DO NOT evaluate whether the claim is true or false. Only understand and decompose the claim.
            DO NOT invent missing facts, names, dates, or locations.

            User claim:
            $claim
        """.trimIndent()
    }
}
