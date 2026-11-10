package com.example.samachaarsathi.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvidenceCollectionTest {

    @Test
    fun allSources_deduplicatesAcrossMultipleAssertionsAndMergesAssertionIds() {
        val sharedSource1 = EvidenceSource(
            id = "src_1",
            title = "PIB Clarification on Laptop Scheme",
            url = "https://pib.gov.in/press/1001",
            relatedAssertionIds = listOf("assertion_1"),
            snippet = "PIB released warning about fake laptop scheme.",
        )
        val sharedSource2 = EvidenceSource(
            id = "src_2",
            title = "PIB Clarification on Laptop Scheme (dup)",
            url = "https://pib.gov.in/press/1001/", // Trailing slash variation
            relatedAssertionIds = listOf("assertion_2"),
        )
        val uniqueSource = EvidenceSource(
            id = "src_3",
            title = "NDTV News Report",
            url = "https://www.ndtv.com/india/laptop-1",
            relatedAssertionIds = listOf("assertion_2"),
        )

        val collection = EvidenceCollection(
            originalClaim = "Government gives laptops and applications open tomorrow",
            assertionEvidence = listOf(
                AssertionEvidence(
                    assertionId = "assertion_1",
                    assertionText = "Government announced free laptops",
                    sources = listOf(sharedSource1),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
                AssertionEvidence(
                    assertionId = "assertion_2",
                    assertionText = "Applications open tomorrow",
                    sources = listOf(sharedSource2, uniqueSource),
                    retrievalStatus = RetrievalStatus.SUCCESS,
                ),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )

        val deduplicated = collection.allSources
        assertEquals(2, deduplicated.size)

        val pib = deduplicated.first { it.domain == "pib.gov.in" }
        assertEquals(listOf("assertion_1", "assertion_2"), pib.relatedAssertionIds)
        assertEquals("PIB released warning about fake laptop scheme.", pib.snippet)

        val ndtv = deduplicated.first { it.domain == "ndtv.com" }
        assertEquals(listOf("assertion_2"), ndtv.relatedAssertionIds)
    }

    @Test
    fun evidenceAvailability_calculatesCorrectlyAcrossStates() {
        val emptyCollection = EvidenceCollection(
            originalClaim = "Test claim",
            assertionEvidence = emptyList(),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )
        assertEquals(EvidenceAvailability.NONE, emptyCollection.evidenceAvailability)

        val failedCollection = EvidenceCollection(
            originalClaim = "Test claim",
            overallRetrievalStatus = RetrievalStatus.FAILED,
        )
        assertEquals(EvidenceAvailability.UNAVAILABLE, failedCollection.evidenceAvailability)

        val inProgressCollection = EvidenceCollection(
            originalClaim = "Test claim",
            overallRetrievalStatus = RetrievalStatus.IN_PROGRESS,
        )
        assertEquals(EvidenceAvailability.PARTIAL, inProgressCollection.evidenceAvailability)

        val source1 = EvidenceSource(id = "1", title = "S1", url = "https://example.com/1")
        val partialEvidence = EvidenceCollection(
            originalClaim = "Compound claim",
            assertionEvidence = listOf(
                AssertionEvidence(assertionId = "1", assertionText = "A1", sources = listOf(source1)),
                AssertionEvidence(assertionId = "2", assertionText = "A2", sources = emptyList()),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )
        assertEquals(EvidenceAvailability.PARTIAL, partialEvidence.evidenceAvailability)

        val source2 = EvidenceSource(id = "2", title = "S2", url = "https://example.com/2")
        val fullEvidence = EvidenceCollection(
            originalClaim = "Compound claim",
            assertionEvidence = listOf(
                AssertionEvidence(assertionId = "1", assertionText = "A1", sources = listOf(source1)),
                AssertionEvidence(assertionId = "2", assertionText = "A2", sources = listOf(source2)),
            ),
            overallRetrievalStatus = RetrievalStatus.SUCCESS,
        )
        assertEquals(EvidenceAvailability.AVAILABLE, fullEvidence.evidenceAvailability)
    }

    @Test
    fun evidenceSource_doesNotFabricatePublicationDate() {
        val source = EvidenceSource(
            id = "src_clean",
            title = "Clean Source",
            url = "https://factcheck.org/story/1",
        )
        assertNull(source.publishedDate)
    }
}
