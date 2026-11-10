package com.example.samachaarsathi.data.model

/**
 * The analytical stance of an evidence source or synthesis towards a specific assertion.
 */
enum class EvidenceStance {
    SUPPORTING,
    CONTRADICTING,
    INCONCLUSIVE,
    INSUFFICIENT_EVIDENCE,
}

/**
 * Objective reliability categorization based on institutional authority and track record.
 * Avoids arbitrary numerical scores.
 */
enum class SourceReliabilityTier {
    OFFICIAL_RECORD,            // Government gazettes, PIB, ministries (.gov, .nic.in)
    INDEPENDENT_FACT_CHECKER,   // Accredited fact-checking newsrooms (AltNews, BoomLive, Snopes)
    CREDIBLE_PRESS,             // Established mainstream news outlets with editorial standards
    GENERAL_WEB,                // Blogs, social links, unverified online reporting
}

/**
 * Assessment of an individual evidence source's relevance and institutional reliability.
 *
 * @property source The original retrieved evidence source.
 * @property reliabilityTier Institutional categorization of the source.
 * @property relevanceScore Transparent qualitative relevance indicator ("DIRECT", "CONTEXTUAL", "TANGENTIAL").
 * @property stance Evaluated stance toward the target assertion.
 * @property reasoning Concise explanation of why this source supports, contradicts, or is inconclusive.
 */
data class SourceAssessment(
    val source: EvidenceSource,
    val reliabilityTier: SourceReliabilityTier = inferReliabilityTier(source),
    val relevanceScore: String = "DIRECT",
    val stance: EvidenceStance = EvidenceStance.INCONCLUSIVE,
    val reasoning: String = "",
) {
    companion object {
        fun inferReliabilityTier(source: EvidenceSource): SourceReliabilityTier {
            return when (source.sourceType) {
                SourceType.OFFICIAL_PORTAL -> SourceReliabilityTier.OFFICIAL_RECORD
                SourceType.FACT_CHECK_ORGANIZATION -> SourceReliabilityTier.INDEPENDENT_FACT_CHECKER
                SourceType.NEWS_MEDIA -> SourceReliabilityTier.CREDIBLE_PRESS
                SourceType.WEB_SEARCH, SourceType.OTHER -> SourceReliabilityTier.GENERAL_WEB
            }
        }
    }
}

/**
 * Represents a direct conflict between two or more sources regarding an assertion.
 *
 * @property assertionId The assertion over which sources disagree.
 * @property supportingSourceIds IDs of sources indicating the assertion is true.
 * @property contradictingSourceIds IDs of sources indicating the assertion is false or debunked.
 * @property explanation Explanation of the nature of the disagreement.
 */
data class EvidenceConflict(
    val assertionId: String,
    val supportingSourceIds: List<String>,
    val contradictingSourceIds: List<String>,
    val explanation: String,
)

/**
 * Documents a specific factual gap where evidence is missing or authoritative confirmation is absent.
 *
 * @property assertionId Identifier of the assertion lacking sufficient evidence.
 * @property missingAspect What specific information or document is missing (e.g. "Official notification", "Gazette copy").
 * @property impactOnVerdict How this gap limits conclusive verification.
 */
data class EvidenceGap(
    val assertionId: String,
    val missingAspect: String,
    val impactOnVerdict: String,
)

/**
 * Comprehensive analysis of a single key assertion against all retrieved evidence.
 *
 * @property assertionId Identifier matching the assertion from ClaimUnderstanding.
 * @property assertionText Full text of the factual assertion.
 * @property stance Aggregated stance across all evaluated sources.
 * @property supportingSources Sources providing affirmative corroboration.
 * @property contradictingSources Sources providing refutation or debunking.
 * @property inconclusiveSources Sources that mention the topic but do not resolve the assertion.
 * @property conflicts Conflicting evidence observed between sources for this assertion.
 * @property gaps Specific missing evidence or unverified aspects.
 * @property summary Analytical narrative summarizing the findings.
 */
data class AssertionAnalysis(
    val assertionId: String,
    val assertionText: String,
    val stance: EvidenceStance,
    val supportingSources: List<SourceAssessment> = emptyList(),
    val contradictingSources: List<SourceAssessment> = emptyList(),
    val inconclusiveSources: List<SourceAssessment> = emptyList(),
    val conflicts: List<EvidenceConflict> = emptyList(),
    val gaps: List<EvidenceGap> = emptyList(),
    val summary: String = "",
) {
    val totalSourcesEvaluated: Int
        get() = supportingSources.size + contradictingSources.size + inconclusiveSources.size

    val hasHighReliabilitySupport: Boolean
        get() = supportingSources.any {
            it.reliabilityTier == SourceReliabilityTier.OFFICIAL_RECORD ||
                it.reliabilityTier == SourceReliabilityTier.INDEPENDENT_FACT_CHECKER
        }

    val hasHighReliabilityContradiction: Boolean
        get() = contradictingSources.any {
            it.reliabilityTier == SourceReliabilityTier.OFFICIAL_RECORD ||
                it.reliabilityTier == SourceReliabilityTier.INDEPENDENT_FACT_CHECKER
        }
}

/**
 * Complete synthesis of evidence analysis for an entire claim and all its decomposed assertions.
 *
 * @property claimUnderstanding Structured understanding produced in Brick 3.
 * @property evidenceCollection Raw evidence retrieved in Brick 4.
 * @property assertionAnalyses Individual analysis per assertion.
 * @property overallStance Aggregated stance across all assertions.
 * @property sourceDiversity Summary of source types encountered (e.g., official, fact-checkers, news).
 * @property totalConflicts Count of conflicting evidence pairs identified.
 * @property totalGaps Count of evidence gaps identified.
 * @property analysisTimestamp Epoch timestamp when analysis occurred.
 */
data class EvidenceAnalysis(
    val claimUnderstanding: ClaimUnderstanding,
    val evidenceCollection: EvidenceCollection,
    val assertionAnalyses: List<AssertionAnalysis> = emptyList(),
    val overallStance: EvidenceStance = EvidenceStance.INSUFFICIENT_EVIDENCE,
    val sourceDiversity: Map<SourceReliabilityTier, Int> = emptyMap(),
    val totalConflicts: Int = 0,
    val totalGaps: Int = 0,
    val analysisTimestamp: Long = System.currentTimeMillis(),
)
