package com.example.samachaarsathi.data.model

import android.net.Uri
import com.example.samachaarsathi.data.image.ForwardingMetadata
import com.example.samachaarsathi.data.image.OcrExtractionResult
import com.example.samachaarsathi.data.image.TextRegion

/**
 * Deterministic test fixtures for offline demonstration, accessibility audits,
 * and physical device verification without live API calls.
 */
object DemoFixtures {

    val sampleSource = EvidenceSource(
        id = "demo_src_1",
        title = "PIB Fact Check: Clarification on Free Laptop Distribution Claim",
        url = "https://pib.gov.in/factcheck/free-laptop-scheme-clarification",
        domain = "pib.gov.in",
        snippet = "PIB Fact Check confirmed no free laptop scheme was approved by the ministry. The viral message links to a fraudulent phishing portal.",
        sourceType = SourceType.OFFICIAL_PORTAL,
        publishedDate = "Nov 4, 2026",
    )

    val sampleSourceSupported = EvidenceSource(
        id = "demo_src_supported",
        title = "ISRO Press Release: SSLV-D3 / EOS-08 Mission Accomplished",
        url = "https://www.isro.gov.in/SSLV_D3_EOS08_Success.html",
        domain = "isro.gov.in",
        snippet = "The third developmental flight of SSLV was successful. The SSLV-D3 placed the EOS-08 satellite into its precise orbit.",
        sourceType = SourceType.OFFICIAL_PORTAL,
        publishedDate = "Aug 16, 2024",
    )

    val sampleUnderstanding = ClaimUnderstanding(
        originalClaim = "Government announced free laptop scheme for college students",
        coreClaim = "Government free laptop distribution scheme",
        claimType = "government announcement",
        entities = listOf("Ministry of Education", "PIB"),
        keyAssertions = listOf("Government distributing free laptops to college students"),
    )

    val sampleEvidence = EvidenceCollection(
        originalClaim = "Government announced free laptop scheme for college students",
        structuredClaim = sampleUnderstanding,
        assertionEvidence = listOf(
            AssertionEvidence(
                assertionId = "1",
                assertionText = "Government distributing free laptops to college students",
                sources = listOf(sampleSource),
                retrievalStatus = RetrievalStatus.SUCCESS,
            ),
        ),
        overallRetrievalStatus = RetrievalStatus.SUCCESS,
    )

    val sampleVerdict = VerificationVerdict(
        category = VerdictCategory.CONTRADICTED,
        headline = "Government has NOT authorized any free laptop scheme",
        reasoning = VerdictReasoning(
            primaryExplanation = "Official releases from the Ministry of Education clarify that this viral claim is fraudulent.",
            keyEvidencePoints = listOf(
                "Official PIB Gazette denies the existence of this scheme",
                "Viral link redirects to an unverified phishing domain",
            ),
            missingContextNotes = listOf(
                "No funds have been sanctioned by the central or state governments for this purpose",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by official government portal and accredited fact-checking records.",
        ),
        claimUnderstanding = sampleUnderstanding,
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "1",
                    assertionText = "Government distributing free laptops to college students",
                    stance = EvidenceStance.CONTRADICTING,
                    summary = "Contradicted by PIB official gazette denial.",
                ),
            ),
        ),
    )

    val sampleVerdictSupported = VerificationVerdict(
        category = VerdictCategory.SUPPORTED,
        headline = "ISRO successfully launched EOS-08 Earth Observation Satellite",
        reasoning = VerdictReasoning(
            primaryExplanation = "Official mission press releases from ISRO confirm that SSLV-D3 placed EOS-08 into intended orbit.",
            keyEvidencePoints = listOf(
                "ISRO official launch bulletin confirms orbital insertion",
                "PIB official release confirms successful satellite deployment",
            ),
            missingContextNotes = listOf(
                "Final developmental test flight before operational commercial missions",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by primary national space agency telemetry.",
        ),
        claimUnderstanding = sampleUnderstanding.copy(
            originalClaim = "ISRO successfully launches new weather observation satellite into orbit",
            coreClaim = "ISRO launches EOS-08 observation satellite",
            entities = listOf("ISRO", "SSLV-D3"),
        ),
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = EvidenceCollection(
                originalClaim = "ISRO successfully launches new weather observation satellite into orbit",
                structuredClaim = sampleUnderstanding,
                assertionEvidence = listOf(
                    AssertionEvidence(
                        assertionId = "1",
                        assertionText = "ISRO launches new satellite",
                        sources = listOf(sampleSourceSupported),
                        retrievalStatus = RetrievalStatus.SUCCESS,
                    ),
                ),
                overallRetrievalStatus = RetrievalStatus.SUCCESS,
            ),
            assertionAnalyses = listOf(
                AssertionAnalysis(
                    assertionId = "1",
                    assertionText = "ISRO launches new satellite",
                    stance = EvidenceStance.SUPPORTING,
                    summary = "Verified by primary ISRO launch release.",
                ),
            ),
        ),
    )

    val sampleVerdictMisleading = VerificationVerdict(
        category = VerdictCategory.MISLEADING_OR_MISSING_CONTEXT,
        headline = "RBI has issued no notification regarding 5000 rupee notes",
        reasoning = VerdictReasoning(
            primaryExplanation = "Fact-checkers have verified that the viral image of a 5000 rupee note is digitally altered.",
            keyEvidencePoints = listOf(
                "Reserve Bank of India official gazette does not list any 5000 denomination",
                "Viral social media image matches an old graphic design mockup",
            ),
            missingContextNotes = listOf(
                "Highest active denomination remains 500 INR",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.HIGH,
            rationale = "Corroborated by RBI official communications portal.",
        ),
        claimUnderstanding = sampleUnderstanding.copy(
            originalClaim = "RBI introduces new 5000 rupee currency notes",
            coreClaim = "RBI circulating new 5000 rupee currency notes",
            entities = listOf("Reserve Bank of India"),
        ),
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = sampleEvidence,
            assertionAnalyses = emptyList(),
        ),
    )

    val sampleVerdictUnverified = VerificationVerdict(
        category = VerdictCategory.UNVERIFIED,
        headline = "Claim of 24-hour weekend metro operations has no corroboration",
        reasoning = VerdictReasoning(
            primaryExplanation = "No official press releases, service advisories, or accredited news reports could confirm extended weekend metro schedules.",
            keyEvidencePoints = listOf(
                "Metro rail official timetable lists standard closing time at 11:00 PM",
                "No transit authority notifications or circulars found",
            ),
            missingContextNotes = listOf(
                "Claim originated from an anonymous social media forward without source links",
            ),
        ),
        confidence = ConfidenceAssessment(
            level = ConfidenceLevel.UNCERTAIN,
            rationale = "Insufficient corroborating public records to evaluate assertion veracity.",
        ),
        claimUnderstanding = sampleUnderstanding.copy(
            originalClaim = "Metro will operate 24 hours this weekend for holiday shoppers",
            coreClaim = "Metro 24 hour weekend schedule",
            entities = listOf("Metro Rail"),
        ),
        evidenceAnalysis = EvidenceAnalysis(
            claimUnderstanding = sampleUnderstanding,
            evidenceCollection = EvidenceCollection(
                originalClaim = "Metro will operate 24 hours this weekend for holiday shoppers",
                structuredClaim = sampleUnderstanding,
                assertionEvidence = emptyList(),
                overallRetrievalStatus = RetrievalStatus.EMPTY_RESULTS,
            ),
            assertionAnalyses = emptyList(),
        ),
    )

    val sampleOcrResult = OcrExtractionResult(
        rawExtractedText = "Forwarded\nGovernment is distributing free laptops to all students in 2026. Apply here: bit.ly/free-laptop-scheme",
        cleanedClaimText = "Government is distributing free laptops to all students in 2026. Apply here: bit.ly/free-laptop-scheme",
        textRegions = listOf(
            TextRegion(text = "Forwarded", lineIndex = 0),
            TextRegion(text = "Government is distributing free laptops to all students in 2026.", lineIndex = 1),
            TextRegion(text = "Apply here: bit.ly/free-laptop-scheme", lineIndex = 2),
        ),
        forwardingMetadata = ForwardingMetadata(
            isForwarded = true,
            headerMarker = "Forwarded",
            containsViralCallToAction = true,
        ),
        hasReadableText = true,
        imageUri = Uri.parse("content://media/external/images/media/sample_claim.png"),
    )
}
