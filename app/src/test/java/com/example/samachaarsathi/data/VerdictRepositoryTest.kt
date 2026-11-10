package com.example.samachaarsathi.data

import com.example.samachaarsathi.data.model.AssertionAnalysis
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.VerdictCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerdictRepositoryTest {

    private lateinit var verdictRepository: DeterministicVerdictRepository

    @Before
    fun setUp() {
        verdictRepository = DeterministicVerdictRepository()
    }

    @Test
    fun determineVerdict_withDebunkingFactCheck_returnsContradictedWithHighConfidence() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Government free laptop scheme",
            coreClaim = "Government provides free laptops",
            claimType = "government announcement",
            keyAssertions = listOf("Free laptop scheme announced"),
        )
        val evidence = EvidenceCollection(originalClaim = understanding.originalClaim)
        val analysis = EvidenceAnalysis(
            claimUnderstanding = understanding,
            evidenceCollection = evidence,
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "assertion_1",
                    assertionText = "Free laptop scheme announced",
                    stance = EvidenceStance.CONTRADICTING,
                    summary = "PIB debunked message as false.",
                ),
            ),
            overallStance = EvidenceStance.CONTRADICTING,
            sourceDiversity = mapOf(SourceReliabilityTier.INDEPENDENT_FACT_CHECKER to 2),
        )

        val result = verdictRepository.determineVerdict(understanding, evidence, analysis)
        assertTrue(result.isSuccess)
        val verdict = result.getOrThrow()

        assertEquals(VerdictCategory.CONTRADICTED, verdict.category)
        assertEquals(ConfidenceLevel.HIGH, verdict.confidence.level)
        assertEquals(listOf("Free laptop scheme announced"), verdict.reasoning.contradictedAssertions)
        assertTrue(verdict.reasoning.supportedAssertions.isEmpty())
    }

    @Test
    fun determineVerdict_withOfficialRecordsAndNoGaps_returnsSupported() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "National scholarship portal launched",
            coreClaim = "Portal launched",
            claimType = "government announcement",
            keyAssertions = listOf("Portal launched"),
        )
        val evidence = EvidenceCollection(originalClaim = understanding.originalClaim)
        val analysis = EvidenceAnalysis(
            claimUnderstanding = understanding,
            evidenceCollection = evidence,
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "assertion_1",
                    assertionText = "Portal launched",
                    stance = EvidenceStance.SUPPORTING,
                    summary = "Confirmed on scholarships.gov.in.",
                ),
            ),
            overallStance = EvidenceStance.SUPPORTING,
            sourceDiversity = mapOf(SourceReliabilityTier.OFFICIAL_RECORD to 1),
        )

        val result = verdictRepository.determineVerdict(understanding, evidence, analysis)
        val verdict = result.getOrThrow()

        assertEquals(VerdictCategory.SUPPORTED, verdict.category)
        assertEquals(ConfidenceLevel.HIGH, verdict.confidence.level)
        assertEquals(listOf("Portal launched"), verdict.reasoning.supportedAssertions)
    }

    @Test
    fun determineVerdict_withSupportedAssertionAndCriticalAmbiguities_returnsMisleadingOrMissingContext() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "50% discount on electricity for all",
            coreClaim = "50% electricity discount",
            claimType = "public policy",
            keyAssertions = listOf("50% subsidy approved"),
            ambiguityNotes = listOf("Scheme only applies to rural agricultural consumers with < 5kW load"),
        )
        val evidence = EvidenceCollection(originalClaim = understanding.originalClaim)
        val analysis = EvidenceAnalysis(
            claimUnderstanding = understanding,
            evidenceCollection = evidence,
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "assertion_1",
                    assertionText = "50% subsidy approved",
                    stance = EvidenceStance.SUPPORTING,
                    summary = "State cabinet passed targeted scheme.",
                ),
            ),
            overallStance = EvidenceStance.SUPPORTING,
            sourceDiversity = mapOf(SourceReliabilityTier.OFFICIAL_RECORD to 1),
        )

        val result = verdictRepository.determineVerdict(understanding, evidence, analysis)
        val verdict = result.getOrThrow()

        assertEquals(VerdictCategory.MISLEADING_OR_MISSING_CONTEXT, verdict.category)
        assertEquals(listOf("Scheme only applies to rural agricultural consumers with < 5kW load"), verdict.reasoning.missingContextNotes)
        assertTrue(verdict.confidence.deductions.isNotEmpty())
    }

    @Test
    fun determineVerdict_withInsufficientEvidence_returnsUnverified() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Obscure forward with no reporting",
            coreClaim = "Obscure forward",
            claimType = "other",
        )
        val evidence = EvidenceCollection(originalClaim = understanding.originalClaim)
        val analysis = EvidenceAnalysis(
            claimUnderstanding = understanding,
            evidenceCollection = evidence,
            assertionAnalyses = emptyList(),
            overallStance = EvidenceStance.INSUFFICIENT_EVIDENCE,
        )

        val result = verdictRepository.determineVerdict(understanding, evidence, analysis)
        val verdict = result.getOrThrow()

        assertEquals(VerdictCategory.UNVERIFIED, verdict.category)
        assertEquals(ConfidenceLevel.UNCERTAIN, verdict.confidence.level)
        assertTrue(verdict.reasoning.supportedAssertions.isEmpty())
        assertTrue(verdict.reasoning.contradictedAssertions.isEmpty())
    }

    @Test
    fun determineVerdict_synthesizesTemporalAndGazetteLimitations() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Scheme opens next month",
            coreClaim = "Scheme opens",
            claimType = "government announcement",
            dateReferences = listOf("next month"),
        )
        val evidence = EvidenceCollection(originalClaim = understanding.originalClaim)
        val analysis = EvidenceAnalysis(
            claimUnderstanding = understanding,
            evidenceCollection = evidence,
            sourceDiversity = mapOf(SourceReliabilityTier.CREDIBLE_PRESS to 1), // no official record
        )

        val result = verdictRepository.determineVerdict(understanding, evidence, analysis)
        val verdict = result.getOrThrow()

        assertFalse(verdict.limitations.isEmpty())
        assertTrue(verdict.limitations.any { it.description.contains("temporal") })
        assertTrue(verdict.limitations.any { it.description.contains("gazette") })
    }
}
