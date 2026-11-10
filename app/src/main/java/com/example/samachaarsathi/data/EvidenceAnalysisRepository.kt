package com.example.samachaarsathi.data

import com.example.samachaarsathi.data.model.AssertionAnalysis
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceConflict
import com.example.samachaarsathi.data.model.EvidenceGap
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.SourceAssessment
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interface for analyzing and cross-referencing retrieved evidence against structured claims.
 */
interface EvidenceAnalysisRepository {
    suspend fun analyzeEvidence(
        understanding: ClaimUnderstanding,
        evidence: EvidenceCollection,
    ): Result<EvidenceAnalysis>
}

/**
 * Deterministic evidence analysis engine that evaluates source diversity, stances, conflicts,
 * and gaps without making live external API calls.
 */
class DeterministicEvidenceAnalysisRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : EvidenceAnalysisRepository {

    override suspend fun analyzeEvidence(
        understanding: ClaimUnderstanding,
        evidence: EvidenceCollection,
    ): Result<EvidenceAnalysis> = withContext(dispatcher) {
        try {
            val assertions = understanding.keyAssertions.ifEmpty {
                listOf(understanding.coreClaim)
            }

            val assertionAnalyses = mutableListOf<AssertionAnalysis>()
            var totalConflicts = 0
            var totalGaps = 0

            for ((index, assertionText) in assertions.withIndex()) {
                val assertionId = "assertion_${index + 1}"
                val matchingAssertionEvidence = evidence.assertionEvidence.find {
                    it.assertionId == assertionId || it.assertionText.equals(assertionText, ignoreCase = true)
                }

                val sources = matchingAssertionEvidence?.sources ?: emptyList()
                val analysis = analyzeSingleAssertion(
                    assertionId = assertionId,
                    assertionText = assertionText,
                    sources = sources,
                )

                assertionAnalyses.add(analysis)
                totalConflicts += analysis.conflicts.size
                totalGaps += analysis.gaps.size
            }

            // Calculate overall source diversity across all assertions
            val diversityMap = mutableMapOf<SourceReliabilityTier, Int>()
            for (source in evidence.allSources) {
                val tier = SourceAssessment.inferReliabilityTier(source)
                diversityMap[tier] = (diversityMap[tier] ?: 0) + 1
            }

            // Aggregate overall stance
            val overallStance = determineOverallStance(assertionAnalyses)

            val analysis = EvidenceAnalysis(
                claimUnderstanding = understanding,
                evidenceCollection = evidence,
                assertionAnalyses = assertionAnalyses,
                overallStance = overallStance,
                sourceDiversity = diversityMap,
                totalConflicts = totalConflicts,
                totalGaps = totalGaps,
            )

            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun analyzeSingleAssertion(
        assertionId: String,
        assertionText: String,
        sources: List<EvidenceSource>,
    ): AssertionAnalysis {
        if (sources.isEmpty()) {
            return AssertionAnalysis(
                assertionId = assertionId,
                assertionText = assertionText,
                stance = EvidenceStance.INSUFFICIENT_EVIDENCE,
                gaps = listOf(
                    EvidenceGap(
                        assertionId = assertionId,
                        missingAspect = "No verifiable web sources discovered",
                        impactOnVerdict = "Assertion cannot be verified without primary sources",
                    ),
                ),
                summary = "No evidence was retrieved for this assertion.",
            )
        }

        val supporting = mutableListOf<SourceAssessment>()
        val contradicting = mutableListOf<SourceAssessment>()
        val inconclusive = mutableListOf<SourceAssessment>()

        for (source in sources) {
            val combinedText = "${source.title} ${source.snippet.orEmpty()}".lowercase()
            val stance = evaluateSourceStance(combinedText)
            val assessment = SourceAssessment(
                source = source,
                stance = stance,
                reasoning = generateReasoning(stance, source),
            )

            when (stance) {
                EvidenceStance.SUPPORTING -> supporting.add(assessment)
                EvidenceStance.CONTRADICTING -> contradicting.add(assessment)
                EvidenceStance.INCONCLUSIVE, EvidenceStance.INSUFFICIENT_EVIDENCE -> inconclusive.add(assessment)
            }
        }

        // Detect conflicts between sources
        val conflicts = mutableListOf<EvidenceConflict>()
        if (supporting.isNotEmpty() && contradicting.isNotEmpty()) {
            conflicts.add(
                EvidenceConflict(
                    assertionId = assertionId,
                    supportingSourceIds = supporting.map { it.source.id },
                    contradictingSourceIds = contradicting.map { it.source.id },
                    explanation = "Direct conflict between affirmative reporting (${supporting.size} sources) and debunking reports (${contradicting.size} sources).",
                ),
            )
        }

        // Identify evidence gaps
        val gaps = mutableListOf<EvidenceGap>()
        val hasOfficial = sources.any { SourceAssessment.inferReliabilityTier(it) == SourceReliabilityTier.OFFICIAL_RECORD }
        val hasFactCheck = sources.any { SourceAssessment.inferReliabilityTier(it) == SourceReliabilityTier.INDEPENDENT_FACT_CHECKER }

        if (!hasOfficial && !hasFactCheck) {
            gaps.add(
                EvidenceGap(
                    assertionId = assertionId,
                    missingAspect = "Authoritative government gazette or independent fact-check corroboration absent",
                    impactOnVerdict = "Relies solely on secondary press or unverified web reporting",
                ),
            )
        }

        val aggregatedStance = when {
            contradicting.isNotEmpty() && supporting.isEmpty() -> EvidenceStance.CONTRADICTING
            contradicting.isNotEmpty() -> {
                // If contradicting sources are authoritative fact-checkers/official and support is only general web
                val highReliabilityContradiction = contradicting.any {
                    it.reliabilityTier == SourceReliabilityTier.INDEPENDENT_FACT_CHECKER ||
                        it.reliabilityTier == SourceReliabilityTier.OFFICIAL_RECORD
                }
                val highReliabilitySupport = supporting.any {
                    it.reliabilityTier == SourceReliabilityTier.OFFICIAL_RECORD
                }
                if (highReliabilityContradiction && !highReliabilitySupport) {
                    EvidenceStance.CONTRADICTING
                } else {
                    EvidenceStance.INCONCLUSIVE
                }
            }
            supporting.isNotEmpty() -> EvidenceStance.SUPPORTING
            else -> EvidenceStance.INCONCLUSIVE
        }

        val summary = when (aggregatedStance) {
            EvidenceStance.CONTRADICTING -> "Found ${contradicting.size} source(s) refuting or debunking this assertion."
            EvidenceStance.SUPPORTING -> "Found ${supporting.size} source(s) corroborating this assertion."
            else -> if (conflicts.isNotEmpty()) "Conflicting reports found." else "Sources mention topic but do not conclusively prove or refute."
        }

        return AssertionAnalysis(
            assertionId = assertionId,
            assertionText = assertionText,
            stance = aggregatedStance,
            supportingSources = supporting,
            contradictingSources = contradicting,
            inconclusiveSources = inconclusive,
            conflicts = conflicts,
            gaps = gaps,
            summary = summary,
        )
    }

    private fun evaluateSourceStance(text: String): EvidenceStance {
        // Refutation / debunking indicators common in Indian fact checks and official press
        val debunkKeywords = listOf(
            "fake", "false", "debunk", "hoax", "fraud", "scam", "misleading",
            "no such scheme", "clarification", "warns against", "untrue",
            "phishing", "fraudulent", "fact check", "viral claim", "bust", "claims false",
        )
        // Affirmative corroboration indicators
        val affirmKeywords = listOf(
            "official notification", "announced scheme", "cabinet approves",
            "launched", "eligibility criteria", "portal opened", "press release",
            "notified", "rollout begins", "applications open",
        )

        val containsDebunk = debunkKeywords.any { text.contains(it) }
        val containsAffirm = affirmKeywords.any { text.contains(it) }

        return when {
            containsDebunk -> EvidenceStance.CONTRADICTING
            containsAffirm -> EvidenceStance.SUPPORTING
            else -> EvidenceStance.INCONCLUSIVE
        }
    }

    private fun generateReasoning(stance: EvidenceStance, source: EvidenceSource): String {
        return when (stance) {
            EvidenceStance.CONTRADICTING -> "Source contains debunking or clarification keywords regarding the claim (${source.domain ?: "web"})."
            EvidenceStance.SUPPORTING -> "Source contains affirmative official announcements or confirmed rollout details (${source.domain ?: "web"})."
            EvidenceStance.INCONCLUSIVE -> "Source discusses related keywords but does not conclusively affirm or refute (${source.domain ?: "web"})."
            EvidenceStance.INSUFFICIENT_EVIDENCE -> "Inconclusive source content."
        }
    }

    private fun determineOverallStance(analyses: List<AssertionAnalysis>): EvidenceStance {
        if (analyses.isEmpty() || analyses.all { it.stance == EvidenceStance.INSUFFICIENT_EVIDENCE }) {
            return EvidenceStance.INSUFFICIENT_EVIDENCE
        }
        if (analyses.any { it.stance == EvidenceStance.CONTRADICTING }) {
            return EvidenceStance.CONTRADICTING
        }
        if (analyses.all { it.stance == EvidenceStance.SUPPORTING }) {
            return EvidenceStance.SUPPORTING
        }
        return EvidenceStance.INCONCLUSIVE
    }
}
