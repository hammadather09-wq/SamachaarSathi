package com.example.samachaarsathi.data.local

import com.example.samachaarsathi.data.model.DemoFixtures
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerdictJsonConverterTest {

    @Test
    fun serializeAndDeserialize_sampleVerdict_preservesAllFields() {
        val original = DemoFixtures.sampleVerdict
        val json = VerdictJsonConverter.serialize(original)
        assertNotNull(json)
        assertTrue(json.isNotBlank())

        val restored = VerdictJsonConverter.deserialize(json)
        assertNotNull(restored)
        assertEquals(original.category, restored!!.category)
        assertEquals(original.headline, restored.headline)
        assertEquals(original.reasoning.primaryExplanation, restored.reasoning.primaryExplanation)
        assertEquals(original.reasoning.supportedAssertions, restored.reasoning.supportedAssertions)
        assertEquals(original.reasoning.contradictedAssertions, restored.reasoning.contradictedAssertions)
        assertEquals(original.confidence.level, restored.confidence.level)
        assertEquals(original.confidence.rationale, restored.confidence.rationale)
        assertEquals(original.confidence.contributingFactors, restored.confidence.contributingFactors)
        assertEquals(original.claimUnderstanding.originalClaim, restored.claimUnderstanding.originalClaim)
        assertEquals(original.claimUnderstanding.coreClaim, restored.claimUnderstanding.coreClaim)
        assertEquals(original.evidenceAnalysis.assertionAnalyses.size, restored.evidenceAnalysis.assertionAnalyses.size)
        assertEquals(original.evidenceAnalysis.evidenceCollection.allSources.size, restored.evidenceAnalysis.evidenceCollection.allSources.size)
    }

    @Test
    fun serializeAndDeserialize_allVerdictVariants_roundtripAccurately() {
        val variants = listOf(
            DemoFixtures.sampleVerdict,
            DemoFixtures.sampleVerdictSupported,
            DemoFixtures.sampleVerdictMisleading,
            DemoFixtures.sampleVerdictUnverified,
        )

        for (variant in variants) {
            val json = VerdictJsonConverter.serialize(variant)
            val restored = VerdictJsonConverter.deserialize(json)
            assertNotNull("Failed roundtrip for ${variant.category}", restored)
            assertEquals(variant.category, restored!!.category)
            assertEquals(variant.headline, restored.headline)
            assertEquals(variant.confidence.level, restored.confidence.level)
            assertEquals(variant.claimUnderstanding.coreClaim, restored.claimUnderstanding.coreClaim)
        }
    }

    @Test
    fun deserialize_emptyOrCorruptJson_returnsNullGracefully() {
        assertNull(VerdictJsonConverter.deserialize(""))
        assertNull(VerdictJsonConverter.deserialize("    "))
        assertNull(VerdictJsonConverter.deserialize("{invalid: json"))
    }

    @Test
    fun entityMapping_toEntityAndToSessionItem_losslessConversion() {
        val originalItem = SessionVerificationItem(
            id = "test_item_42",
            claimText = "Government announced free laptop scheme for college students",
            timestampMs = 1716000000000L,
            verdict = DemoFixtures.sampleVerdict,
            isSaved = true,
        )

        val entity = originalItem.toEntity()
        assertEquals("test_item_42", entity.id)
        assertEquals(originalItem.claimText, entity.claimText)
        assertEquals(1716000000000L, entity.timestampMs)
        assertTrue(entity.isSaved)
        assertEquals(VerdictCategory.CONTRADICTED.name, entity.verdictCategory)
        assertEquals(originalItem.verdict?.headline, entity.headline)
        assertNotNull(entity.verdictJson)

        val restoredItem = entity.toSessionVerificationItem()
        assertEquals(originalItem.id, restoredItem.id)
        assertEquals(originalItem.claimText, restoredItem.claimText)
        assertEquals(originalItem.timestampMs, restoredItem.timestampMs)
        assertTrue(restoredItem.isSaved)
        assertNotNull(restoredItem.verdict)
        assertEquals(originalItem.verdict?.category, restoredItem.verdict?.category)
        assertEquals(originalItem.verdict?.headline, restoredItem.verdict?.headline)
    }
}
