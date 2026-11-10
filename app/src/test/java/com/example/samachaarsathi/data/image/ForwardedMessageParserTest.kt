package com.example.samachaarsathi.data.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardedMessageParserTest {

    @Test
    fun parse_emptyLines_returnsEmptyAndNotForwarded() {
        val (cleaned, meta) = ForwardedMessageParser.parse(emptyList())

        assertEquals("", cleaned)
        assertFalse(meta.isForwarded)
        assertFalse(meta.containsViralCallToAction)
    }

    @Test
    fun parse_blankLines_returnsEmptyAndNotForwarded() {
        val (cleaned, meta) = ForwardedMessageParser.parse(listOf("  ", "\t", "\n"))

        assertEquals("", cleaned)
        assertFalse(meta.isForwarded)
    }

    @Test
    fun parse_englishForwardedHeader_stripsHeaderAndDetectsForwarded() {
        val lines = listOf(
            "Forwarded",
            "Government is giving free laptops to all college students in India.",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertEquals("Government is giving free laptops to all college students in India.", cleaned)
        assertTrue(meta.isForwarded)
        assertEquals("Forwarded", meta.headerMarker)
        assertFalse(meta.containsViralCallToAction)
    }

    @Test
    fun parse_forwardedManyTimes_stripsHeader() {
        val lines = listOf(
            "Forwarded many times",
            "New education policy 2026 cancels all 10th board exams.",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertEquals("New education policy 2026 cancels all 10th board exams.", cleaned)
        assertTrue(meta.isForwarded)
        assertEquals("Forwarded many times", meta.headerMarker)
    }

    @Test
    fun parse_hindiForwardedHeader_stripsHindiHeader() {
        val lines = listOf(
            "अग्रषित",
            "सरकार सभी कॉलेज छात्रों को मुफ्त लैपटॉप बांट रही है।",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertEquals("सरकार सभी कॉलेज छात्रों को मुफ्त लैपटॉप बांट रही है।", cleaned)
        assertTrue(meta.isForwarded)
        assertEquals("अग्रषित", meta.headerMarker)
    }

    @Test
    fun parse_standaloneTimestamp_stripsTimestampAndCapturesIt() {
        val lines = listOf(
            "10:45 AM",
            "Reserve Bank has withdrawn 500 rupee notes from circulation.",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertEquals("Reserve Bank has withdrawn 500 rupee notes from circulation.", cleaned)
        assertEquals("10:45 AM", meta.timestampDetected)
    }

    @Test
    fun parse_viralCallToAction_detectsViralCta() {
        val lines = listOf(
            "Free recharge scheme announced.",
            "Forward this to 10 friends immediately to activate offer.",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertTrue(meta.isForwarded)
        assertTrue(meta.containsViralCallToAction)
        assertTrue(cleaned.contains("Free recharge scheme announced."))
    }

    @Test
    fun parse_hindiViralCallToAction_detectsViralCta() {
        val lines = listOf(
            "यह संदेश तुरंत सब मित्रों को भेजो",
            "सबको भेजो और लाभ उठाओ",
        )
        val (_, meta) = ForwardedMessageParser.parse(lines)

        assertTrue(meta.containsViralCallToAction)
    }

    @Test
    fun parse_plainMessageWithoutMetadata_preservesTextAndFlagsFalse() {
        val lines = listOf(
            "ISRO launched a new earth observation satellite from Sriharikota.",
        )
        val (cleaned, meta) = ForwardedMessageParser.parse(lines)

        assertEquals("ISRO launched a new earth observation satellite from Sriharikota.", cleaned)
        assertFalse(meta.isForwarded)
        assertFalse(meta.containsViralCallToAction)
    }

    @Test
    fun parse_multipleClaims_detectsMultipleClaims() {
        val lines = listOf(
            "1. Free laptop scheme starts tomorrow.",
            "2. 500 rupee notes discontinued.",
            "3. Petrol tax reduced by 50%.",
            "4. Free electricity for all businesses.",
        )
        val (_, meta) = ForwardedMessageParser.parse(lines)

        assertTrue(meta.multipleClaimsDetected)
    }
}
