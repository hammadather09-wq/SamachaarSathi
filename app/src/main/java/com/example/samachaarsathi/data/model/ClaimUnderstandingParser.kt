package com.example.samachaarsathi.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust JSON parser and validator for Gemini Claim Understanding responses.
 */
object ClaimUnderstandingParser {

    /**
     * Parses raw JSON string returned by Gemini into a validated [ClaimUnderstanding].
     *
     * Safely handles markdown code blocks, unexpected wrapping text, missing fields,
     * and malformed JSON structures without crashing.
     *
     * @param rawJson The raw JSON response string from Gemini.
     * @param originalClaim The user's exact original claim.
     * @return [Result.success] with the populated [ClaimUnderstanding], or [Result.failure] if JSON is malformed.
     */
    fun parse(rawJson: String, originalClaim: String): Result<ClaimUnderstanding> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty response received for claim understanding"))
        }

        return try {
            val cleanedJson = extractJsonPayload(trimmed)
            val jsonObject = JSONObject(cleanedJson)

            val coreClaim = jsonObject.optString("coreClaim").trim()
                .takeIf { it.isNotEmpty() }
                ?: originalClaim.trim()

            val claimType = jsonObject.optString("claimType").trim()
                .takeIf { it.isNotEmpty() }
                ?: "other"

            val entities = jsonObject.optJSONArray("entities").toStringList()
            val dateReferences = jsonObject.optJSONArray("dateReferences").toStringList()
            val locationReferences = jsonObject.optJSONArray("locationReferences").toStringList()

            var keyAssertions = jsonObject.optJSONArray("keyAssertions").toStringList()
            if (keyAssertions.isEmpty()) {
                keyAssertions = listOf(coreClaim)
            }

            val evidenceRequirements = jsonObject.optJSONArray("evidenceRequirements").toStringList()
            val ambiguityNotes = jsonObject.optJSONArray("ambiguityNotes").toStringList()
            val language = jsonObject.optString("language").trim().takeIf { it.isNotEmpty() }

            val understanding = ClaimUnderstanding(
                originalClaim = originalClaim,
                coreClaim = coreClaim,
                claimType = claimType,
                entities = entities,
                dateReferences = dateReferences,
                locationReferences = locationReferences,
                keyAssertions = keyAssertions,
                evidenceRequirements = evidenceRequirements,
                ambiguityNotes = ambiguityNotes,
                language = language,
            )
            Result.success(understanding)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Failed to parse claim understanding JSON: ${e.message}", e))
        }
    }

    private fun extractJsonPayload(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }

        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if ((firstBrace != -1) && (lastBrace != -1) && (lastBrace > firstBrace)) {
            return text.substring(firstBrace, lastBrace + 1)
        }
        return text
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            val item = optString(i, "").trim()
            if (item.isNotEmpty()) {
                list.add(item)
            }
        }
        return list
    }
}
