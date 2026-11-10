package com.example.samachaarsathi.ui.share

import com.example.samachaarsathi.data.model.AssertionEvidence
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.ConfidenceAssessment
import com.example.samachaarsathi.data.model.ConfidenceLevel
import com.example.samachaarsathi.data.model.EvidenceAnalysis
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.EvidenceSource
import com.example.samachaarsathi.data.model.RetrievalStatus
import com.example.samachaarsathi.data.model.SourceType
import com.example.samachaarsathi.data.model.VerdictCategory
import com.example.samachaarsathi.data.model.VerdictReasoning
import com.example.samachaarsathi.data.model.VerificationVerdict
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareHelperTest {

    private val sampleSource = EvidenceSource(
        id = "src_1",
        title = "PIB Official Gazette Denial",
        url = "https://pib.gov.in/factcheck/denial-123",
        domain = "pib.gov.in",
        snippet = "No free laptop scheme has been approved by the Government.",
        sourceType = SourceType.OFFICIAL_PORTAL,
    )

    private val sampleUnderstanding = ClaimUnderstanding(
        originalClaim = "Government announced free laptop scheme",
        coreClaim = "Free laptop scheme announcement",
        claimType = "government announcement",
    )

    private val sampleEvidence = EvidenceCollection(
        originalClaim = "Government announced free laptop scheme",
        structuredClaim = sampleUnderstanding,
        assertionEvidence = listOf(
            AssertionEvidence(
                assertionId = "1",
                assertionText = "Government announced free laptop scheme",
                sources = listOf(sampleSource),
                retrievalStatus = RetrievalStatus.SUCCESS,
            ),
        ),
        overallRetrievalStatus = RetrievalStatus.SUCCESS,
    )

    private val sampleVerdict = VerificationVerdict(
        category = VerdictCategory.CONTRADICTED,
        headline = "Government has NOT authorized any free laptop scheme",
        reasoning = VerdictReasoning(
            primaryExplanation = "Official releases from the Ministry of Education clarify that this viral claim is fraudulent.",
            keyEvidencePoints = listOf("Official website has no record of the scheme"),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by official gazette denial",
        ),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
        ),
    )

    @Test
    fun buildShareText_includesAllRequiredFields() {
        val shareText = ShareHelper.buildShareText(
            claim = "Government announced free laptop scheme",
            verdict = sampleVerdict,
            timestampMs = 1730000000000L,
        )

        // 1. Branding
        assertTrue(shareText.contains("SamachaarSathi Fact-Check Report"))

        // 2. Claim
        assertTrue(shareText.contains("\"Government announced free laptop scheme\""))

        // 3. Verdict
        assertTrue(shareText.contains("VERIFIED FALSE"))

        // 4. Confidence
        assertTrue(shareText.contains("High Confidence"))

        // 5. Headline & Explanation
        assertTrue(shareText.contains("Government has NOT authorized any free laptop scheme"))
        assertTrue(shareText.contains("Official releases from the Ministry of Education"))

        // 6. Grounded Source
        assertTrue(shareText.contains("pib.gov.in"))
        assertTrue(shareText.contains("https://pib.gov.in/factcheck/denial-123"))

        // 7. Limitation note
        assertTrue(shareText.contains("This assessment reflects evidence retrieved"))
    }

    @Test
    fun buildShareText_handlesNullVerdictGracefully() {
        val shareText = ShareHelper.buildShareText(
            claim = "Unverified breaking message",
            verdict = null,
        )

        assertTrue(shareText.contains("SamachaarSathi Fact-Check Report"))
        assertTrue(shareText.contains("\"Unverified breaking message\""))
        assertTrue(shareText.contains("Captured for verification"))
        assertTrue(shareText.contains("This assessment reflects evidence retrieved"))
    }
}
