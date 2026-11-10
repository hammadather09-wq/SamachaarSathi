package com.example.samachaarsathi.data

import android.util.Log
import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.GroundingMetadataParser
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface for retrieving grounded web evidence using Google Search Grounding.
 */
interface EvidenceRetrievalRepository {
    suspend fun retrieveEvidenceForUnderstanding(understanding: ClaimUnderstanding): Result<EvidenceCollection>
    suspend fun retrieveEvidenceForAssertion(assertionId: String, assertionText: String): Result<AssertionEvidence>
}

/**
 * Implementation of [EvidenceRetrievalRepository] using Firebase AI Logic's Google Search Grounding.
 *
 * Configured with `gemini-3.8-flash`, the Gemini Developer API backend, and [Tool.googleSearch].
 *
 * NOTE: Initialized lazily to ensure zero network connections until explicitly requested.
 */
class FirebaseEvidenceRetrievalRepository(
    private val modelProvider: () -> GenerativeModel = {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-3.8-flash",
                tools = listOf(Tool.googleSearch()),
            )
    },
) : EvidenceRetrievalRepository {

    private val model: GenerativeModel by lazy { modelProvider() }

    override suspend fun retrieveEvidenceForAssertion(
        assertionId: String,
        assertionText: String,
    ): Result<AssertionEvidence> = withContext(Dispatchers.IO) {
        val trimmedAssertion = assertionText.trim()
        if (trimmedAssertion.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Assertion text cannot be blank"))
        }

        val prompt = buildAssertionSearchPrompt(trimmedAssertion)
        try {
            Log.d(TAG, "Executing Google Search Grounding for assertion $assertionId (length: ${trimmedAssertion.length})...")
            val response = model.generateContent(prompt)
            val candidate = response.candidates.firstOrNull()
            val groundingMetadata = candidate?.groundingMetadata
            val responseText = response.text

            Log.d(
                TAG,
                "Google Search Grounding completed for $assertionId. " +
                    "Queries: ${groundingMetadata?.webSearchQueries?.size ?: 0}, " +
                    "Chunks: ${groundingMetadata?.groundingChunks?.size ?: 0}",
            )

            val evidence = GroundingMetadataParser.parse(
                metadata = groundingMetadata,
                assertionId = assertionId,
                assertionText = trimmedAssertion,
                groundedResponseText = responseText,
            )
            Result.success(evidence)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving evidence for assertion $assertionId", e)
            Result.failure(e)
        }
    }

    override suspend fun retrieveEvidenceForUnderstanding(
        understanding: ClaimUnderstanding,
    ): Result<EvidenceCollection> = withContext(Dispatchers.IO) {
        val assertions = understanding.keyAssertions.ifEmpty {
            listOf(understanding.coreClaim)
        }

        val assertionEvidenceList = mutableListOf<AssertionEvidence>()
        val warnings = mutableListOf<String>()

        for ((index, assertionText) in assertions.withIndex()) {
            val assertionId = "assertion_${index + 1}"
            val result = retrieveEvidenceForAssertion(assertionId, assertionText)
            result.fold(
                onSuccess = { assertionEvidenceList.add(it) },
                onFailure = { error ->
                    warnings.add("Failed to retrieve evidence for assertion ${index + 1}: ${error.localizedMessage}")
                    assertionEvidenceList.add(
                        AssertionEvidence(
                            assertionId = assertionId,
                            assertionText = assertionText,
                            sources = emptyList(),
                            retrievalStatus = RetrievalStatus.FAILED,
                            groundingMetadata = null,
                            searchQueries = emptyList(),
                            groundedResponseText = null,
                        ),
                    )
                },
            )
        }

        val overallStatus = when {
            assertionEvidenceList.all { it.retrievalStatus == RetrievalStatus.FAILED } -> RetrievalStatus.FAILED
            assertionEvidenceList.all { it.retrievalStatus == RetrievalStatus.EMPTY_RESULTS } -> RetrievalStatus.EMPTY_RESULTS
            assertionEvidenceList.any { it.retrievalStatus == RetrievalStatus.SUCCESS } -> RetrievalStatus.SUCCESS
            else -> RetrievalStatus.FAILED
        }

        Result.success(
            EvidenceCollection(
                originalClaim = understanding.originalClaim,
                structuredClaim = understanding,
                assertionEvidence = assertionEvidenceList,
                overallRetrievalStatus = overallStatus,
                errorsOrWarnings = warnings,
            ),
        )
    }

    companion object {
        private const val TAG = "FirebaseEvidenceRepo"

        fun buildAssertionSearchPrompt(assertion: String): String = """
            Search the web to find factual sources and reporting related to this specific claim:
            "$assertion"

            Focus on finding official announcements, credible reporting, government press releases, or fact checks.
            Summarize the relevant facts found in the sources.
        """.trimIndent()
    }
}
