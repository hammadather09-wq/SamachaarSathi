package com.example.samachaarsathi.data.image

/**
 * Parser for analyzing forwarded message patterns, chat screenshot artifacts, and viral call-to-actions.
 * Operates purely locally and never assigns truth values based on message origin.
 */
object ForwardedMessageParser {

    private val FORWARD_HEADERS = listOf(
        Regex("(?i)^\\s*forwarded(?:\\s+many\\s+times)?\\s*$"),
        Regex("(?i)^\\s*forwarded\\s+as\\s+received\\s*$"),
        Regex("(?i)^\\s*अग्रषित\\s*$"),
        Regex("(?i)^\\s*फारवर्ड\\s*$"),
        Regex("(?i)^\\s*urgent\\s+forward\\s*:?"),
    )

    private val VIRAL_CTA_PATTERNS = listOf(
        Regex("(?i)forward\\s+(?:this\\s+)?to\\s+(?:all|\\d+)"),
        Regex("(?i)share\\s+(?:with|to)\\s+(?:all|every|your\\s+family)"),
        Regex("(?i)don'?t\\s+delete\\s+without\\s+sharing"),
        Regex("(?i)sabko\\s+bhejo"),
        Regex("(?i)viral\\s+karo"),
        Regex("(?i)सबको\\s+भेजो"),
        Regex("(?i)शेयर\\s+करें"),
        Regex("(?i)फॉरवर्ड\\s+करें"),
        Regex("(?i)वायरल\\s+करो"),
    )

    private val TIMESTAMP_REGEX = Regex("(?i)\\[?\\b\\d{1,2}[:.]\\d{2}(?:\\s*[ap]m)?\\b]?")

    /**
     * Parses raw OCR lines, identifies forwarding markers, and separates them from factual claims.
     */
    fun parse(rawLines: List<String>): Pair<String, ForwardingMetadata> {
        val nonBlank = rawLines.map { it.trim() }.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) {
            return Pair("", ForwardingMetadata(isForwarded = false))
        }

        var headerFound: String? = null
        var timestampFound: String? = null
        var containsCta = false
        val cleanedLines = mutableListOf<String>()

        for (line in nonBlank) {
            // Check for forward header
            val matchesHeader = FORWARD_HEADERS.any { it.containsMatchIn(line) }
            if (matchesHeader) {
                if (headerFound == null) headerFound = line
                continue
            }

            // Check for standalone timestamp line
            if (TIMESTAMP_REGEX.matches(line)) {
                if (timestampFound == null) timestampFound = line
                continue
            }

            // Check for inline timestamp
            val tsMatch = TIMESTAMP_REGEX.find(line)
            if (tsMatch != null && timestampFound == null) {
                timestampFound = tsMatch.value
            }

            // Check for viral call to action
            if (VIRAL_CTA_PATTERNS.any { it.containsMatchIn(line) }) {
                containsCta = true
            }

            cleanedLines.add(line)
        }

        val cleanedText = cleanedLines.joinToString("\n").trim()
        val hasMultipleClaims = cleanedText.contains("?") ||
            cleanedText.contains("1.") ||
            cleanedLines.size >= 4

        val metadata = ForwardingMetadata(
            isForwarded = headerFound != null || containsCta,
            headerMarker = headerFound,
            timestampDetected = timestampFound,
            containsViralCallToAction = containsCta,
            multipleClaimsDetected = hasMultipleClaims,
        )

        return Pair(cleanedText.ifBlank { nonBlank.joinToString("\n") }, metadata)
    }
}
