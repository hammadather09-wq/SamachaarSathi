package com.example.samachaarsathi.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaimUnderstandingParserTest {

    @Test
    fun parse_singleFactualClaimExtraction_succeeds() {
        val originalClaim = "WHO declared global emergency over disease outbreak"
        val json = """
            {
                "coreClaim": "WHO declared a global health emergency over a disease outbreak",
                "claimType": "health",
                "entities": ["World Health Organization", "WHO"],
                "dateReferences": [],
                "locationReferences": ["global"],
                "keyAssertions": ["The World Health Organization declared a global health emergency"],
                "evidenceRequirements": ["Official WHO press release or Director-General declaration"],
                "ambiguityNotes": ["Specific disease name is not mentioned"],
                "language": "en"
            }
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals(originalClaim, understanding.originalClaim)
        assertEquals("WHO declared a global health emergency over a disease outbreak", understanding.coreClaim)
        assertEquals("health", understanding.claimType)
        assertEquals(listOf("World Health Organization", "WHO"), understanding.entities)
        assertTrue(understanding.dateReferences.isEmpty())
        assertEquals(listOf("global"), understanding.locationReferences)
        assertEquals(1, understanding.keyAssertions.size)
        assertEquals("The World Health Organization declared a global health emergency", understanding.keyAssertions[0])
        assertEquals(1, understanding.evidenceRequirements.size)
        assertEquals(1, understanding.ambiguityNotes.size)
        assertEquals("en", understanding.language)
    }

    @Test
    fun parse_multipleAssertionsExtraction_identifiesAllSeparateStatements() {
        val originalClaim = "The government announced free laptops for college students and applications open next month."
        val json = """
            {
                "coreClaim": "Government announced a free laptop scheme with applications opening next month",
                "claimType": "government announcement",
                "entities": ["Government", "College students"],
                "dateReferences": ["next month"],
                "locationReferences": [],
                "keyAssertions": [
                    "The government has announced a scheme to distribute free laptops to college students.",
                    "Applications for the laptop scheme will open next month."
                ],
                "evidenceRequirements": [
                    "Official government gazette notification or ministry press release",
                    "Official application portal circular or notification"
                ],
                "ambiguityNotes": [
                    "Does not specify which government (central or state)",
                    "Does not specify eligibility criteria or eligible colleges"
                ],
                "language": "en"
            }
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals(2, understanding.keyAssertions.size)
        assertEquals("The government has announced a scheme to distribute free laptops to college students.", understanding.keyAssertions[0])
        assertEquals("Applications for the laptop scheme will open next month.", understanding.keyAssertions[1])
        assertEquals(listOf("next month"), understanding.dateReferences)
        assertTrue(understanding.locationReferences.isEmpty())
        assertEquals(2, understanding.ambiguityNotes.size)
    }

    @Test
    fun parse_ambiguousClaim_recordsMissingContextInAmbiguityNotes() {
        val originalClaim = "They are giving free laptops."
        val json = """
            {
                "coreClaim": "Unspecified entity is distributing free laptops",
                "claimType": "other",
                "entities": ["laptops"],
                "dateReferences": [],
                "locationReferences": [],
                "keyAssertions": ["Free laptops are being distributed"],
                "evidenceRequirements": ["Official announcement or source from the distributing entity"],
                "ambiguityNotes": [
                    "Entity 'They' is completely unspecified (could be government, company, or NGO)",
                    "Target recipient group is not specified",
                    "Geographical location and jurisdiction are not mentioned",
                    "No timeline, eligibility, or application process is provided"
                ],
                "language": "en"
            }
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals("Unspecified entity is distributing free laptops", understanding.coreClaim)
        assertTrue(understanding.dateReferences.isEmpty())
        assertTrue(understanding.locationReferences.isEmpty())
        assertEquals(4, understanding.ambiguityNotes.size)
        assertTrue(understanding.ambiguityNotes.any { it.contains("unspecified", ignoreCase = true) })
    }

    @Test
    fun parse_missingEntitiesDatesAndLocations_returnsEmptyLists() {
        val originalClaim = "Drinking boiled water cures viral infection"
        val json = """
            {
                "coreClaim": "Drinking boiled water cures viral infections",
                "claimType": "health",
                "keyAssertions": ["Drinking boiled water acts as a cure for viral infections"]
            }
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals("health", understanding.claimType)
        assertTrue(understanding.entities.isEmpty())
        assertTrue(understanding.dateReferences.isEmpty())
        assertTrue(understanding.locationReferences.isEmpty())
        assertTrue(understanding.evidenceRequirements.isEmpty())
        assertTrue(understanding.ambiguityNotes.isEmpty())
        assertNull(understanding.language)
    }

    @Test
    fun parse_markdownWrappedJson_extractsAndParsesSuccessfully() {
        val originalClaim = "NASA confirms alien signals detected"
        val json = """
            ```json
            {
                "coreClaim": "NASA confirmed detection of extraterrestrial signals",
                "claimType": "science",
                "entities": ["NASA"],
                "dateReferences": [],
                "locationReferences": [],
                "keyAssertions": ["NASA confirmed detecting alien signals"],
                "evidenceRequirements": ["Official NASA statement or scientific publication"],
                "ambiguityNotes": [],
                "language": "en"
            }
            ```
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals("science", understanding.claimType)
        assertEquals(listOf("NASA"), understanding.entities)
    }

    @Test
    fun parse_emptyStringResponse_returnsFailure() {
        val result = ClaimUnderstandingParser.parse("", "Any claim")
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun parse_whitespaceOnlyResponse_returnsFailure() {
        val result = ClaimUnderstandingParser.parse("   \n\t  ", "Any claim")
        assertTrue(result.isFailure)
    }

    @Test
    fun parse_malformedJson_returnsFailureWithoutCrashing() {
        val malformed = """{"coreClaim": "Incomplete json string"""
        val result = ClaimUnderstandingParser.parse(malformed, "Any claim")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun parse_missingCoreClaim_fallsBackToOriginalClaim() {
        val originalClaim = "Breaking news headline claim"
        val json = """
            {
                "claimType": "politics",
                "keyAssertions": ["Political assertion statement"]
            }
        """.trimIndent()

        val result = ClaimUnderstandingParser.parse(json, originalClaim)
        assertTrue(result.isSuccess)
        val understanding = result.getOrThrow()

        assertEquals(originalClaim, understanding.coreClaim)
        assertEquals("politics", understanding.claimType)
    }
}
