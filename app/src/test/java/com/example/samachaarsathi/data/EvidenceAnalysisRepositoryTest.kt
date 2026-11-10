package com.example.samachaarsathi.data

import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.EvidenceStance
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceReliabilityTier
import com.example.samachaarsathi.data.model.SourceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvidenceAnalysisRepositoryTest {

    private lateinit var repository: DeterministicEvidenceAnalysisRepository

    @Before
    fun setUp() {
        repository = DeterministicEvidenceAnalysisRepository()
    }

    @Test
    fun analyzeEvidence_withDebunkingSources_yieldsContradictingStance() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Government is giving free laptops to college students",
            coreClaim = "Government announces free laptops for students",
            claimType = "government announcement",
            keyAssertions = listOf("Government announced free laptops scheme for students"),
        )

        val debunkSource = EvidenceSource(
            id = "src_1",
            title = "PIB Fact Check: Viral Message on Free Laptop Scheme is Fake",
            url = "https://pib.gov.in/factcheck/123",
            domain = "pib.gov.in",
            snippet = "PIB clarifies that no such scheme has been notified by the government.",
            sourceType = SourceType.OFFICIAL_PORTAL,
            relatedAssertionIds = listOf("assertion_1"),
        )

        val evidence = EvidenceCollection(
            originalClaim = understanding.originalClaim,
            structuredClaim = understanding,
            assertionEvidence = listOf(
                AssertionEvidence(
                    assertionId = "assertion_1",
                    assertionText = understanding.keyAssertions[0],
                    sources = listOf(debunkSource),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )

        val result = repository.analyzeEvidence(understanding, evidence)
        assertTrue(result.isSuccess)
        val analysis = result.getOrThrow()

        assertEquals(EvidenceStance.CONTRADICTING, analysis.overallStance)
        assertEquals(1, analysis.assertionAnalyses.size)
        assertEquals(EvidenceStance.CONTRADICTING, analysis.assertionAnalyses[0].stance)
        assertEquals(1, analysis.assertionAnalyses[0].contradictingSources.size)
        assertTrue(analysis.assertionAnalyses[0].hasHighReliabilityContradiction)
    }

    @Test
    fun analyzeEvidence_withOfficialLaunchAnnouncement_yieldsSupportingStance() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Government launched national scholarship portal",
            coreClaim = "Government launched national scholarship portal",
            claimType = "government announcement",
            keyAssertions = listOf("National scholarship portal launched by government"),
        )

        val officialSource = EvidenceSource(
            id = "src_official",
            title = "Official Notification: Cabinet Approves National Scholarship Portal",
            url = "https://scholarships.gov.in/notification",
            domain = "scholarships.gov.in",
            snippet = "Official notification confirmed portal opened for applications.",
            sourceType = SourceType.OFFICIAL_PORTAL,
            relatedAssertionIds = listOf("assertion_1"),
        )

        val evidence = EvidenceCollection(
            originalClaim = understanding.originalClaim,
            structuredClaim = understanding,
            assertionEvidence = listOf(
                AssertionEvidence(
                    assertionId = "assertion_1",
                    assertionText = understanding.keyAssertions[0],
                    sources = listOf(officialSource),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )

        val result = repository.analyzeEvidence(understanding, evidence)
        assertTrue(result.isSuccess)
        val analysis = result.getOrThrow()

        assertEquals(EvidenceStance.SUPPORTING, analysis.overallStance)
        assertEquals(1, analysis.assertionAnalyses[0].supportingSources.size)
        assertTrue(analysis.assertionAnalyses[0].hasHighReliabilitySupport)
    }

    @Test
    fun analyzeEvidence_withConflictingReports_flagsConflictAndInconclusiveStance() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Government gives subsidy to tea farmers",
            coreClaim = "Government tea farmer subsidy",
            claimType = "public policy",
            keyAssertions = listOf("Subsidy approved for tea farmers"),
        )

        val supportivePress = EvidenceSource(
            id = "src_news",
            title = "State announced scheme for tea growers",
            url = "https://localnews.in/tea-subsidy",
            snippet = "State launched initial pilot program for tea growers.",
            sourceType = SourceType.NEWS_MEDIA,
        )
        val factCheckDebunk = EvidenceSource(
            id = "src_factcheck",
            title = "Viral message claims fake subsidy - BoomLive",
            url = "https://boomlive.in/tea-subsidy-fake",
            snippet = "No cabinet approval exists and circular is false.",
            sourceType = SourceType.FACT_CHECK_ORGANIZATION,
        )

        val evidence = EvidenceCollection(
            originalClaim = understanding.originalClaim,
            structuredClaim = understanding,
            assertionEvidence = listOf(
                AssertionEvidence(
                    assertionId = "assertion_1",
                    assertionText = understanding.keyAssertions[0],
                    sources = listOf(supportivePress, factCheckDebunk),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )

        val result = repository.analyzeEvidence(understanding, evidence)
        val analysis = result.getOrThrow()

        assertEquals(1, analysis.totalConflicts)
        assertEquals(1, analysis.assertionAnalyses[0].conflicts.size)
        // High reliability fact check overrides general news, or marks as inconclusive/contradicting
        assertTrue(
            analysis.assertionAnalyses[0].stance == EvidenceStance.CONTRADICTING ||
                analysis.assertionAnalyses[0].stance == EvidenceStance.INCONCLUSIVE,
        )
    }

    @Test
    fun analyzeEvidence_withEmptyEvidence_recordsGapsAndInsufficientStance() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Unheard claim with zero web coverage",
            coreClaim = "Unheard claim",
            claimType = "other",
            keyAssertions = listOf("Unheard claim"),
        )

        val emptyEvidence = EvidenceCollection(
            originalClaim = understanding.originalClaim,
            structuredClaim = understanding,
            assertionEvidence = emptyList(),
            overallRetrievalStatus = RetrievalStatus.EMPTY_RESULTS,
        )

        val result = repository.analyzeEvidence(understanding, emptyEvidence)
        val analysis = result.getOrThrow()

        assertEquals(EvidenceStance.INSUFFICIENT_EVIDENCE, analysis.overallStance)
        assertEquals(1, analysis.totalGaps)
        assertEquals(1, analysis.assertionAnalyses[0].gaps.size)
    }

    @Test
    fun analyzeEvidence_tracksSourceDiversityAccurately() = runBlocking {
        val understanding = ClaimUnderstanding(
            originalClaim = "Test claim",
            coreClaim = "Test claim",
            claimType = "government announcement",
        )

        val evidence = EvidenceCollection(
            originalClaim = "Test claim",
            assertionEvidence = listOf(
                AssertionEvidence(
                    assertionId = "assertion_1",
                    assertionText = "Test claim",
                    sources = listOf(
                        EvidenceSource(id = "1", title = "P1", url = "https://pib.gov.in/1", sourceType = SourceType.OFFICIAL_PORTAL),
                        EvidenceSource(id = "2", title = "P2", url = "https://ndtv.com/1", sourceType = SourceType.NEWS_MEDIA),
                        EvidenceSource(id = "3", title = "P3", url = "https://boomlive.in/1", sourceType = SourceType.FACT_CHECK_ORGANIZATION),
                        EvidenceSource(id = "4", title = "P4", url = "https://randomblog.com/1", sourceType = SourceType.WEB_SEARCH),
                    ),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
            ),
        )

        val result = repository.analyzeEvidence(understanding, evidence)
        val analysis = result.getOrThrow()

        assertEquals(1, analysis.sourceDiversity[SourceReliabilityTier.OFFICIAL_RECORD])
        assertEquals(1, analysis.sourceDiversity[SourceReliabilityTier.CREDIBLE_PRESS])
        assertEquals(1, analysis.sourceDiversity[SourceReliabilityTier.INDEPENDENT_FACT_CHECKER])
        assertEquals(1, analysis.sourceDiversity[SourceReliabilityTier.GENERAL_WEB])
    }
}
