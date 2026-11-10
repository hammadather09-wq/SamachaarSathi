package com.example.samachaarsathi.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository interface for communicating with Gemini.
 */
interface GeminiRepository {
    fun analyzeClaimStream(claim: String): Flow<String>
    suspend fun analyzeClaim(claim: String): Result<String>
}

/**
 * Implementation of [GeminiRepository] using Firebase AI Logic with the Gemini Developer API backend.
 */
class FirebaseGeminiRepository(
    private val model: GenerativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName = "gemini-3.8-flash"),
) : GeminiRepository {

    override fun analyzeClaimStream(claim: String): Flow<String> = flow {
        val trimmedClaim = claim.trim()
        if (trimmedClaim.isBlank()) {
            throw IllegalArgumentException("Claim cannot be empty or blank")
        }

        val prompt = """
            You are the AI engine of SamachaarSathi. Analyze the following user claim and provide a concise preliminary assessment. Do not claim that the information is verified because this request does not yet have web-search evidence. Clearly state uncertainty.

            User claim:
            $trimmedClaim
        """.trimIndent()

        Log.d(TAG, "Calling model.generateContentStream for prompt length ${prompt.length}...")
        var chunkCount = 0
        model.generateContentStream(prompt).collect { response ->
            val chunkText = response.text
            if (!chunkText.isNullOrEmpty()) {
                chunkCount++
                Log.d(TAG, "Received chunk #$chunkCount (length: ${chunkText.length})")
                emit(chunkText)
            }
        }
        if (chunkCount == 0) {
            Log.w(TAG, "Gemini stream completed without emitting any text chunks")
            throw IllegalStateException("No response text returned by the model")
        }
        Log.d(TAG, "model.generateContentStream completed successfully with $chunkCount chunks")
    }.flowOn(Dispatchers.IO)

    override suspend fun analyzeClaim(claim: String): Result<String> {
        return try {
            val fullResponse = StringBuilder()
            analyzeClaimStream(claim).collect { chunk ->
                fullResponse.append(chunk)
            }
            val text = fullResponse.toString().trim()
            if (text.isNotBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("No response text returned by the model"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeClaim", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FirebaseGeminiRepository"
    }
}
