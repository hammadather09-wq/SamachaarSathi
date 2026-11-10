package com.example.samachaarsathi.data

import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationLimitation
import com.example.samachaarsathi.data.model.VerificationVerdict
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface for synthesizing structured claim understanding, retrieved evidence,
 * and cross-referencing analysis into an explainable verification verdict.
 */
interface VerdictRepository {
    suspend fun determineVerdict(
        understanding: ClaimUnderstanding,
        evidence: EvidenceCollection,
        analysis: EvidenceAnalysis,
    ): Result<VerificationVerdict>
}

/**
 * Deterministic verdict engine operating purely on evidence synthesis without live AI inference.
 */
class DeterministicVerdictRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : VerdictRepository {

    override suspend fun determineVerdict(
        understanding: ClaimUnderstanding,
        evidence: EvidenceCollection,
        analysis: EvidenceAnalysis,
    ): Result<VerificationVerdict> = withContext(dispatcher) {
        try {
            val supported = mutableListOf<String>()
            val contradicted = mutableListOf<String>()
            val inconclusive = mutableListOf<String>()

            for (aAnalysis in analysis.assertionAnalyses) {
                when (aAnalysis.stance) {
                    EvidenceStance.SUPPORTING -> supported.add(aAnalysis.assertionText)
                    EvidenceStance.CONTRADICTING -> contradicted.add(aAnalysis.assertionText)
                    EvidenceStance.INCONCLUSIVE, EvidenceStance.INSUFFICIENT_EVIDENCE -> inconclusive.add(aAnalysis.assertionText)
                }
            }

            val hasGaps = analysis.totalGaps > 0
            val hasConflicts = analysis.totalConflicts > 0
            val hasAmbiguities = understanding.ambiguityNotes.isNotEmpty()

            // 1. Determine Verdict Category
            val category: VerdictCategory = when {
                analysis.assertionAnalyses.isEmpty() || analysis.overallStance == EvidenceStance.INSUFFICIENT_EVIDENCE -> {
                    VerdictCategory.UNVERIFIED
                }
                contradicted.isNotEmpty() && supported.isNotEmpty() -> {
                    VerdictCategory.MISLEADING_OR_MISSING_CONTEXT
                }
                contradicted.isNotEmpty() -> {
                    VerdictCategory.CONTRADICTED
                }
                supported.isNotEmpty() && (hasAmbiguities || hasGaps || hasConflicts) -> {
                    VerdictCategory.MISLEADING_OR_MISSING_CONTEXT
                }
                supported.isNotEmpty() && inconclusive.isEmpty() -> {
                    VerdictCategory.SUPPORTED
                }
                else -> {
                    VerdictCategory.UNVERIFIED
                }
            }

            // 2. Determine Headline
            val headline = when (category) {
                VerdictCategory.SUPPORTED -> "Claim Confirmed by Official Reporting"
                VerdictCategory.CONTRADICTED -> "Claim Refuted / Debunked by Credible Sources"
                VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> "Claim is Misleading or Missing Critical Context"
                VerdictCategory.UNVERIFIED -> "Claim Cannot Be Verified with Available Evidence"
            }

            // 3. Determine Confidence Assessment
            val confidence = calculateConfidence(category, analysis, understanding)

            // 4. Synthesize Limitations
            val limitations = mutableListOf<VerificationLimitation>()
            if (understanding.dateReferences.isNotEmpty() && understanding.dateReferences.any { it.contains("next") || it.contains("tomorrow") }) {
                limitations.add(
                    VerificationLimitation(
                        description = "Relative temporal references (e.g. 'next month') cannot be permanently anchored.",
                        impact = "Announcement dates require verification against formal gazette schedules.",
                    ),
                )
            }
            if (analysis.sourceDiversity[SourceReliabilityTier.OFFICIAL_RECORD] == null) {
                limitations.add(
                    VerificationLimitation(
                        description = "Primary government gazette or official ministry circular not directly indexed.",
                        impact = "Finding is grounded in secondary journalistic or fact-check reporting.",
                    ),
                )
            }

            // 5. Build Reasoning Narrative
            val keyPoints = mutableListOf<String>()
            for (a in analysis.assertionAnalyses) {
                if (a.summary.isNotBlank()) {
                    keyPoints.add("${a.assertionText}: ${a.summary}")
                }
            }

            val primaryExplanation = when (category) {
                VerdictCategory.SUPPORTED ->
                    "The factual assertions in this claim are corroborated by affirmative reporting from credible sources."
                VerdictCategory.CONTRADICTED ->
                    "Credible reporting and fact-check records confirm that the central assertion is false, fraudulent, or non-existent."
                VerdictCategory.MISLEADING_OR_MISSING_CONTEXT ->
                    "While some elements of the claim may relate to real initiatives, critical details, caveats, or eligibility limits are omitted or distorted."
                VerdictCategory.UNVERIFIED ->
                    "No authoritative public records, official press releases, or independent fact-checks were found to verify or debunk this claim."
            }

            val reasoning = VerdictReasoning(
                primaryExplanation = primaryExplanation,
                supportedAssertions = supported,
                contradictedAssertions = contradicted,
                missingContextNotes = understanding.ambiguityNotes,
                keyEvidencePoints = keyPoints,
            )

            val verdict = VerificationVerdict(
                category = category,
                headline = headline,
                reasoning = reasoning,
                confidence = confidence,
                limitations = limitations,
                claimUnderstanding = understanding,
                evidenceAnalysis = analysis,
            )

            Result.success(verdict)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateConfidence(
        category: VerdictCategory,
        analysis: EvidenceAnalysis,
        understanding: ClaimUnderstanding,
    ): ConfidenceAssessment {
        val factors = mutableListOf<String>()
        val deductions = mutableListOf<String>()

        val officialCount = analysis.sourceDiversity[SourceReliabilityTier.OFFICIAL_RECORD] ?: 0
        val factCheckCount = analysis.sourceDiversity[SourceReliabilityTier.INDEPENDENT_FACT_CHECKER] ?: 0
        val pressCount = analysis.sourceDiversity[SourceReliabilityTier.CREDIBLE_PRESS] ?: 0

        if (officialCount > 0) factors.add("Direct corroboration from official government portals ($officialCount source(s))")
        if (factCheckCount > 0) factors.add("Independent fact-checking confirmation ($factCheckCount source(s))")
        if (pressCount >= 2) factors.add("Multi-source journalistic consensus ($pressCount news sources)")

        if (understanding.ambiguityNotes.isNotEmpty()) deductions.add("Ambiguities in original claim text reduce precision")
        if (analysis.totalConflicts > 0) deductions.add("Conflicting reporting observed between sources")
        if (analysis.totalGaps > 0) deductions.add("Evidentiary gaps present across key assertions")

        val level = when (category) {
            VerdictCategory.UNVERIFIED -> ConfidenceLevel.UNCERTAIN
            VerdictCategory.CONTRADICTED -> {
                if (officialCount > 0 || factCheckCount > 0) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM
            }
            VerdictCategory.SUPPORTED -> {
                if (officialCount > 0 && deductions.isEmpty()) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM
            }
            VerdictCategory.MISLEADING_OR_MISSING_CONTEXT -> {
                if (factors.isNotEmpty()) ConfidenceLevel.MEDIUM else ConfidenceLevel.LOW
            }
        }

        val rationale = when (level) {
            ConfidenceLevel.HIGH -> "High confidence based on official government records or authoritative fact-checking investigations."
            ConfidenceLevel.MEDIUM -> "Moderate confidence based on consistent secondary reporting with minor caveats."
            ConfidenceLevel.LOW -> "Low confidence due to conflicting reports or unverified web claims."
            ConfidenceLevel.UNCERTAIN -> "Certainty cannot be established due to an absence of corroborating evidence."
        }

        return ConfidenceAssessment(
            level = level,
            rationale = rationale,
            contributingFactors = factors,
            deductions = deductions,
        )
    }
}
