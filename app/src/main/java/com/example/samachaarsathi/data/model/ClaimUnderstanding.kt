package com.example.samachaarsathi.data.model

/**
 * Structured representation of a claim produced by the Claim Understanding Engine (Brick 3).
 *
 * Decomposes a raw user claim into core factual assertions, entities, temporal/spatial
 * references, verification evidence requirements, and ambiguity notes for evidence retrieval.
 *
 * @property originalClaim Exact original user input.
 * @property coreClaim Main factual assertion in clear, concise language.
 * @property claimType Appropriate category (e.g. government announcement, public policy, health, finance, science, technology, politics, other).
 * @property entities People, organizations, government departments, companies, products explicitly identified.
 * @property dateReferences Dates or time periods mentioned in the claim.
 * @property locationReferences Places or geographical references mentioned.
 * @property keyAssertions Individual factual statements that can be independently checked.
 * @property evidenceRequirements Information or primary sources required to verify each assertion.
 * @property ambiguityNotes Missing, unspecified, or unclear details (without inventing answers).
 * @property language Detected language of the original claim (e.g., "en", "hi", "hinglish").
 */
data class ClaimUnderstanding(
    val originalClaim: String,
    val coreClaim: String,
    val claimType: String,
    val entities: List<String> = emptyList(),
    val dateReferences: List<String> = emptyList(),
    val locationReferences: List<String> = emptyList(),
    val keyAssertions: List<String> = emptyList(),
    val evidenceRequirements: List<String> = emptyList(),
    val ambiguityNotes: List<String> = emptyList(),
    val language: String? = null,
)
