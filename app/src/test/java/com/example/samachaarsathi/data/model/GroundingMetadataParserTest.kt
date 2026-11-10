package com.example.samachaarsathi.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroundingMetadataParserTest {

    @Test
    fun parseDto_withValidWebChunks_extractsSourcesWithCorrectDomainsAndTitles() {
        val dto = GroundingMetadataDto(
            webSearchQueries = listOf("government free laptop scheme 2024", "PIB fact check laptop"),
            searchEntryPointHtml = "<div>Google Search</div>",
            groundingChunks = listOf(
                GroundingChunkDto(
                    webUri = "https://pib.gov.in/PressReleasePage.aspx?PRID=199999",
                    webTitle = "PIB Press Release: Free Laptop Scheme Clarification",
                    webDomain = "pib.gov.in",
                ),
                GroundingChunkDto(
                    webUri = "https://www.ndtv.com/education/fake-news-alert-free-laptop-scheme-viral-message-debunked-5000000",
                    webTitle = "Fake News Alert: Free Laptop Scheme Debunked - NDTV",
                    webDomain = "ndtv.com",
                ),
            ),
            groundingSupports = emptyList(),
        )

        val result = GroundingMetadataParser.parseDto(
            dto = dto,
            assertionId = "assertion_1",
            assertionText = "Government is distributing free laptops for college students",
        )

        assertEquals("assertion_1", result.assertionId)
        assertEquals(RetrievalStatus.SUCCESS, result.retrievalStatus)
        assertEquals(2, result.sources.size)

        val pibSource = result.sources.first { it.url.contains("pib.gov.in") }
        assertEquals("PIB Press Release: Free Laptop Scheme Clarification", pibSource.title)
        assertEquals("pib.gov.in", pibSource.domain)
        assertEquals(SourceType.OFFICIAL_PORTAL, pibSource.sourceType)
        assertEquals(listOf("assertion_1"), pibSource.relatedAssertionIds)
        assertNull("Publication dates must never be fabricated when not provided", pibSource.publishedDate)

        val ndtvSource = result.sources.first { it.url.contains("ndtv.com") }
        assertEquals("ndtv.com", ndtvSource.domain)
        assertEquals(SourceType.NEWS_MEDIA, ndtvSource.sourceType)
    }

    @Test
    fun parseDto_withGroundingSupports_associatesSnippetWithCorrectChunkIndex() {
        val dto = GroundingMetadataDto(
            webSearchQueries = listOf("laptop scheme fact check"),
            groundingChunks = listOf(
                GroundingChunkDto(
                    webUri = "https://www.boomlive.in/fact-check/free-laptop-scheme-whatsapp-forward-fake-12345",
                    webTitle = "Viral Message Claiming Free Laptops Is Fake: BOOM",
                    webDomain = "boomlive.in",
                ),
                GroundingChunkDto(
                    webUri = "https://timesofindia.indiatimes.com/india/fake-laptop-scheme/articleshow/123.cms",
                    webTitle = "No Such Free Laptop Scheme Announced - TOI",
                    webDomain = "timesofindia.indiatimes.com",
                ),
            ),
            groundingSupports = listOf(
                GroundingSupportDto(
                    textSegment = "The Press Information Bureau confirmed that no such scheme exists and the viral link is a phishing scam.",
                    groundingChunkIndices = listOf(0),
                ),
            ),
        )

        val result = GroundingMetadataParser.parseDto(
            dto = dto,
            assertionId = "assertion_1",
            assertionText = "Government laptop scheme",
        )

        val boomSource = result.sources.first { it.url.contains("boomlive.in") }
        assertEquals(SourceType.FACT_CHECK_ORGANIZATION, boomSource.sourceType)
        assertEquals(
            "The Press Information Bureau confirmed that no such scheme exists and the viral link is a phishing scam.",
            boomSource.snippet,
        )

        val toiSource = result.sources.first { it.url.contains("timesofindia") }
        assertNull("Chunk without support mapping should have null snippet", toiSource.snippet)
    }

    @Test
    fun parseDto_withNullOrEmptyDto_returnsEmptyResultsStatusWithoutThrowing() {
        val resultNull = GroundingMetadataParser.parseDto(
            dto = null,
            assertionId = "assertion_1",
            assertionText = "Test claim",
        )
        assertEquals(RetrievalStatus.EMPTY_RESULTS, resultNull.retrievalStatus)
        assertTrue(resultNull.sources.isEmpty())

        val resultEmpty = GroundingMetadataParser.parseDto(
            dto = GroundingMetadataDto(),
            assertionId = "assertion_1",
            assertionText = "Test claim",
        )
        assertEquals(RetrievalStatus.EMPTY_RESULTS, resultEmpty.retrievalStatus)
        assertTrue(resultEmpty.sources.isEmpty())
    }

    @Test
    fun parseDto_deduplicatesSameUrlsWithinAssertion() {
        val dto = GroundingMetadataDto(
            groundingChunks = listOf(
                GroundingChunkDto(
                    webUri = "https://pib.gov.in/factcheck/1",
                    webTitle = "PIB Fact Check 1",
                ),
                GroundingChunkDto(
                    webUri = "https://pib.gov.in/factcheck/1/", // Trailing slash variation
                    webTitle = "PIB Fact Check Duplicate",
                ),
            ),
        )

        val result = GroundingMetadataParser.parseDto(
            dto = dto,
            assertionId = "assertion_1",
            assertionText = "Duplicate test",
        )

        assertEquals(1, result.sources.size)
        assertEquals("PIB Fact Check 1", result.sources[0].title)
    }

    @Test
    fun parseDto_ignoresChunksWithMissingOrBlankUri() {
        val dto = GroundingMetadataDto(
            groundingChunks = listOf(
                GroundingChunkDto(webUri = null, webTitle = "Missing URI"),
                GroundingChunkDto(webUri = "   ", webTitle = "Blank URI"),
                GroundingChunkDto(webUri = "https://valid.org/info", webTitle = "Valid Source"),
            ),
        )

        val result = GroundingMetadataParser.parseDto(
            dto = dto,
            assertionId = "assertion_1",
            assertionText = "Valid test",
        )

        assertEquals(1, result.sources.size)
        assertEquals("https://valid.org/info", result.sources[0].url)
    }

    @Test
    fun parseFromJson_parsesCompleteGoogleSearchGroundingJson() {
        val json = """
            {
              "webSearchQueries": ["free laptop college students", "PIB laptop fact check"],
              "searchEntryPoint": {
                "renderedContent": "<div>Search Entry Point</div>"
              },
              "groundingChunks": [
                {
                  "web": {
                    "uri": "https://pib.gov.in/PressReleasePage.aspx?PRID=12345",
                    "title": "PIB Clarifies Free Laptop Scheme",
                    "domain": "pib.gov.in"
                  }
                },
                {
                  "web": {
                    "uri": "https://altnews.in/viral-claim-free-laptop-students",
                    "title": "Viral Claim on Free Laptops is False - Alt News",
                    "domain": "altnews.in"
                  }
                }
              ],
              "groundingSupports": [
                {
                  "segment": {
                    "text": "The government has issued multiple warnings that the message is fraudulent."
                  },
                  "groundingChunkIndices": [0, 1]
                }
              ]
            }
        """.trimIndent()

        val parseResult = GroundingMetadataParser.parseFromJson(
            jsonString = json,
            assertionId = "assertion_1",
            assertionText = "Government laptop claim",
        )

        assertTrue(parseResult.isSuccess)
        val evidence = parseResult.getOrThrow()
        assertEquals(RetrievalStatus.SUCCESS, evidence.retrievalStatus)
        assertEquals(2, evidence.sources.size)
        assertEquals(2, evidence.searchQueries.size)

        val altNews = evidence.sources.first { it.domain == "altnews.in" }
        assertEquals(SourceType.FACT_CHECK_ORGANIZATION, altNews.sourceType)
        assertNotNull(altNews.snippet)
    }

    @Test
    fun parseFromJson_handlesMalformedJsonGracefully() {
        val parseResult = GroundingMetadataParser.parseFromJson(
            jsonString = "{ malformed json :: not valid [",
            assertionId = "assertion_1",
            assertionText = "Error test",
        )

        assertTrue(parseResult.isFailure)
    }

    @Test
    fun parseFromJson_emptyOrBlankStringReturnsEmptyResults() {
        val resultBlank = GroundingMetadataParser.parseFromJson(
            jsonString = "   \n\t  ",
            assertionId = "assertion_1",
            assertionText = "Blank test",
        )

        assertTrue(resultBlank.isSuccess)
        assertEquals(RetrievalStatus.EMPTY_RESULTS, resultBlank.getOrThrow().retrievalStatus)
    }

    @Test
    fun extractDomain_handlesVariousFormats() {
        assertEquals("pib.gov.in", EvidenceSource.extractDomain("https://pib.gov.in/press/1"))
        assertEquals("ndtv.com", EvidenceSource.extractDomain("https://www.ndtv.com/news/123"))
        assertEquals("example.org", EvidenceSource.extractDomain("http://example.org:8080/path?query=1"))
        assertEquals("factcheck.org", EvidenceSource.extractDomain("factcheck.org/article"))
        assertNull(EvidenceSource.extractDomain(null))
        assertNull(EvidenceSource.extractDomain(""))
    }

    @Test
    fun inferSourceType_classifiesCorrectly() {
        assertEquals(SourceType.OFFICIAL_PORTAL, EvidenceSource.inferSourceType("https://pib.gov.in"))
        assertEquals(SourceType.OFFICIAL_PORTAL, EvidenceSource.inferSourceType("https://education.gov.in"))
        assertEquals(SourceType.OFFICIAL_PORTAL, EvidenceSource.inferSourceType("https://scholarships.gov.in"))
        assertEquals(SourceType.FACT_CHECK_ORGANIZATION, EvidenceSource.inferSourceType("https://www.altnews.in/fact-check"))
        assertEquals(SourceType.FACT_CHECK_ORGANIZATION, EvidenceSource.inferSourceType("https://www.boomlive.in"))
        assertEquals(SourceType.NEWS_MEDIA, EvidenceSource.inferSourceType("https://www.thehindu.com/news"))
        assertEquals(SourceType.NEWS_MEDIA, EvidenceSource.inferSourceType("https://timesofindia.indiatimes.com"))
        assertEquals(SourceType.WEB_SEARCH, EvidenceSource.inferSourceType("https://randomblog.com/post/1"))
    }
}
