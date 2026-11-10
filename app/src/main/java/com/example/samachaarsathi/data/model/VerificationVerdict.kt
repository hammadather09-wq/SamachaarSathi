package com.example.samachaarsathi.data.model

/**
 * Standard verdict categories for claim verification.
 */
enum class VerdictCategory {
    SUPPORTED,
    CONTRADICTED,
    MISLEADING_OR_MISSING_CONTEXT,
    UNVERIFIED,
}

/**
 * Qualitative confidence assessment level based on evidence quality and source independence.
 */
enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
    UNCERTAIN,
}

/**
 * Transparent explanation of confidence calculation based on concrete evidence criteria.
 *
 * @property level Qualitative level (HIGH, MEDIUM, LOW, UNCERTAIN).
 * @property rationale Clear explanation of why this level was assigned.
 * @property contributingFactors Concrete criteria met (e.g. "Primary official gazette verified").
 * @property deductions Concrete risk factors that reduced confidence (e.g. "Missing eligibility criteria").
 */
data class ConfidenceAssessment(
    val level: ConfidenceLevel,
    val rationale: String,
    val contributingFactors: List<String> = emptyList(),
    val deductions: List<String> = emptyList(),
)

/**
 * Specific evidentiary or temporal limitation of the verification verdict.
 *
 * @property description Limitation description (e.g. "Regional applicability not confirmed").
 * @property impact How this limitation affects the certainty of the verdict.
 */
data class VerificationLimitation(
    val description: String,
    val impact: String,
)

/**
 * Transparent analytical reasoning backing the verdict.
 *
 * @property primaryExplanation Human-readable narrative explaining the verdict.
 * @property supportedAssertions Assertions confirmed by evidence.
 * @property contradictedAssertions Assertions refuted by evidence.
 * @property missingContextNotes Important omissions or context gaps.
 * @property keyEvidencePoints Salient factual points extracted from sources.
 */
data class VerdictReasoning(
    val primaryExplanation: String,
    val supportedAssertions: List<String> = emptyList(),
    val contradictedAssertions: List<String> = emptyList(),
    val missingContextNotes: List<String> = emptyList(),
    val keyEvidencePoints: List<String> = emptyList(),
)

/**
 * Complete verification verdict for a claim.
 *
 * @property category Verdict classification (SUPPORTED, CONTRADICTED, MISLEADING_OR_MISSING_CONTEXT, UNVERIFIED).
 * @property headline High-level headline for quick comprehension.
 * @property reasoning Detailed explanatory reasoning.
 * @property confidence Transparent confidence assessment.
 * @property limitations Explicit limitations of the finding.
 * @property claimUnderstanding Decomposed claim from Brick 3.
 * @property evidenceAnalysis Deep analysis from Brick 5.
 * @property timestamp Epoch timestamp when the verdict was generated.
 */
data class VerificationVerdict(
    val category: VerdictCategory,
    val headline: String,
    val reasoning: VerdictReasoning,
    val confidence: ConfidenceAssessment,
    val limitations: List<VerificationLimitation> = emptyList(),
    val claimUnderstanding: ClaimUnderstanding,
    val evidenceAnalysis: EvidenceAnalysis,
    val timestamp: Long = System.currentTimeMillis(),
)
