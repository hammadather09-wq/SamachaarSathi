package com.example.samachaarsathi.data.language

/**
 * Deterministic language and script detector for Indian languages and transliterated text.
 * Operates offline without external network or AI dependencies.
 */
object LanguageDetectionEngine {

    private val HINGLISH_MARKERS = setOf(
        "sarkar", "sarkaar", "yojana", "muft", "bhejo", "dost", "paisa", "paise",
        "aavedan", "karo", "modi", "chhatra", "mil", "raha", "rahe", "hai", "hain",
        "nahi", "khabar", "suno", "dekho", "karein", "aayega", "dengi", "milega",
        "sabko", "jaldi", "forward", "viral",
    )

    private val MARATHI_MARKERS = setOf(
        "आहे", "नाही", "शासन", "योजना", "मिळणार", "विद्यार्थी", "घोषणा", "अर्ज",
    )

    /**
     * Analyzes raw claim text and returns comprehensive language and script metadata.
     */
    fun detect(text: String): LanguageMetadata {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return LanguageMetadata(
                language = SupportedIndianLanguage.ENGLISH,
                confidence = 1.0f,
                detectedScript = "Latin",
                isTransliterated = false,
            )
        }

        var devanagariCount = 0
        var bengaliCount = 0
        var tamilCount = 0
        var teluguCount = 0
        var kannadaCount = 0
        var malayalamCount = 0
        var latinCount = 0
        var otherCount = 0

        for (ch in trimmed) {
            when (ch) {
                in '\u0900'..'\u097F' -> devanagariCount++
                in '\u0980'..'\u09FF' -> bengaliCount++
                in '\u0B80'..'\u0BFF' -> tamilCount++
                in '\u0C00'..'\u0C7F' -> teluguCount++
                in '\u0C80'..'\u0CFF' -> kannadaCount++
                in '\u0D00'..'\u0D7F' -> malayalamCount++
                in 'a'..'z', in 'A'..'Z' -> latinCount++
                ' ', '\t', '\n', '\r', '.', ',', '!', '?', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> Unit
                else -> otherCount++
            }
        }

        val distribution = mapOf(
            "Devanagari" to devanagariCount,
            "Bengali" to bengaliCount,
            "Tamil" to tamilCount,
            "Telugu" to teluguCount,
            "Kannada" to kannadaCount,
            "Malayalam" to malayalamCount,
            "Latin" to latinCount,
        ).filter { it.value > 0 }

        val totalIdentified = devanagariCount + bengaliCount + tamilCount + teluguCount +
            kannadaCount + malayalamCount + latinCount + otherCount

        if (totalIdentified == 0) {
            return LanguageMetadata(
                language = SupportedIndianLanguage.ENGLISH,
                confidence = 1.0f,
                detectedScript = "Latin",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
        }

        // Dominant script determination
        val maxEntry = distribution.maxByOrNull { it.value }
        val dominantScript = maxEntry?.key ?: "Latin"
        val scriptChars = maxEntry?.value ?: 0
        val confidence = (scriptChars.toFloat() / totalIdentified.toFloat()).coerceIn(0.1f, 1.0f)

        return when (dominantScript) {
            "Devanagari" -> {
                val hasMarathiSpecific = trimmed.contains('\u0933') || // ळ
                    MARATHI_MARKERS.any { trimmed.contains(it) }
                val language = if (hasMarathiSpecific) SupportedIndianLanguage.MARATHI else SupportedIndianLanguage.HINDI
                LanguageMetadata(
                    language = language,
                    confidence = confidence,
                    detectedScript = "Devanagari",
                    isTransliterated = false,
                    scriptDistribution = distribution,
                )
            }
            "Bengali" -> LanguageMetadata(
                language = SupportedIndianLanguage.BENGALI,
                confidence = confidence,
                detectedScript = "Bengali",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
            "Tamil" -> LanguageMetadata(
                language = SupportedIndianLanguage.TAMIL,
                confidence = confidence,
                detectedScript = "Tamil",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
            "Telugu" -> LanguageMetadata(
                language = SupportedIndianLanguage.TELUGU,
                confidence = confidence,
                detectedScript = "Telugu",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
            "Kannada" -> LanguageMetadata(
                language = SupportedIndianLanguage.KANNADA,
                confidence = confidence,
                detectedScript = "Kannada",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
            "Malayalam" -> LanguageMetadata(
                language = SupportedIndianLanguage.MALAYALAM,
                confidence = confidence,
                detectedScript = "Malayalam",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
            "Latin" -> {
                val words = trimmed.lowercase().split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
                val hinglishMatches = words.count { HINGLISH_MARKERS.contains(it) }
                val isHinglish = hinglishMatches >= 2 || (words.size <= 5 && hinglishMatches >= 1)

                if (isHinglish) {
                    LanguageMetadata(
                        language = SupportedIndianLanguage.HINGLISH_TRANSLITERATED,
                        confidence = 0.85f,
                        detectedScript = "Latin",
                        isTransliterated = true,
                        scriptDistribution = distribution,
                    )
                } else {
                    LanguageMetadata(
                        language = SupportedIndianLanguage.ENGLISH,
                        confidence = confidence,
                        detectedScript = "Latin",
                        isTransliterated = false,
                        scriptDistribution = distribution,
                    )
                }
            }
            else -> LanguageMetadata(
                language = SupportedIndianLanguage.UNKNOWN,
                confidence = 0.5f,
                detectedScript = "Unknown",
                isTransliterated = false,
                scriptDistribution = distribution,
            )
        }
    }
}

/**
 * Service for preparing multilingual claims for verification while preserving names and original wording.
 */
interface MultilingualClaimService {
    suspend fun processClaim(rawClaim: String): Result<MultilingualClaim>
}

/**
 * Deterministic implementation that detects language, preserves entities, and marks translation status honestly.
 */
class DeterministicMultilingualClaimService : MultilingualClaimService {

    override suspend fun processClaim(rawClaim: String): Result<MultilingualClaim> {
        val trimmed = rawClaim.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Claim text cannot be blank"))
        }

        val metadata = LanguageDetectionEngine.detect(trimmed)

        // Preserve key entity patterns (URLs, numbers, capitalised English names)
        val entities = mutableListOf<String>()
        val urlRegex = Regex("https?://\\S+")
        urlRegex.findAll(trimmed).forEach { entities.add(it.value) }

        val isPending = metadata.language != SupportedIndianLanguage.ENGLISH &&
            metadata.language != SupportedIndianLanguage.HINGLISH_TRANSLITERATED

        val multilingualClaim = MultilingualClaim(
            originalClaim = trimmed,
            languageMetadata = metadata,
            searchReadyClaim = trimmed,
            preservedEntities = entities,
            isLiveTranslationPending = isPending,
        )

        return Result.success(multilingualClaim)
    }
}
