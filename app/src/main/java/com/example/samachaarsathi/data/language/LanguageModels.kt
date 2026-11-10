package com.example.samachaarsathi.data.language

/**
 * Enumeration of supported official and widely used Indian languages.
 */
enum class SupportedIndianLanguage(
    val code: String,
    val nativeName: String,
    val scriptName: String,
) {
    ENGLISH("en", "English", "Latin"),
    HINDI("hi", "हिन्दी", "Devanagari"),
    MARATHI("mr", "मराठी", "Devanagari"),
    TELUGU("te", "తెలుగు", "Telugu"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada"),
    MALAYALAM("ml", "മലയാളം", "Malayalam"),
    BENGALI("bn", "বাংলা", "Bengali"),
    HINGLISH_TRANSLITERATED("hi-Latn", "Hinglish / Romanized Indic", "Latin"),
    UNKNOWN("und", "Unknown", "Other"),
}

/**
 * Metadata about the detected language and script of the input claim.
 *
 * @property language Detected language category.
 * @property confidence Confidence of detection (0.0 to 1.0).
 * @property detectedScript Identified primary Unicode script.
 * @property isTransliterated Whether Roman/Latin script was used for an Indic language.
 * @property scriptDistribution Character count per detected script family.
 */
data class LanguageMetadata(
    val language: SupportedIndianLanguage,
    val confidence: Float,
    val detectedScript: String,
    val isTransliterated: Boolean = false,
    val scriptDistribution: Map<String, Int> = emptyMap(),
)

/**
 * Representation of a claim with language awareness and preserved original text.
 *
 * @property originalClaim Exact raw input entered by the user.
 * @property languageMetadata Language and script detection metadata.
 * @property searchReadyClaim Representation prepared for search grounding.
 * @property preservedEntities Named entities, schemes, or dates preserved without loss.
 * @property isLiveTranslationPending True when neural translation is marked pending for production AI deployment.
 */
data class MultilingualClaim(
    val originalClaim: String,
    val languageMetadata: LanguageMetadata,
    val searchReadyClaim: String,
    val preservedEntities: List<String> = emptyList(),
    val isLiveTranslationPending: Boolean = false,
)
