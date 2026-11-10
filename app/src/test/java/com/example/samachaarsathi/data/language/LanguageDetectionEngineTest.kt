package com.example.samachaarsathi.data.language

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageDetectionEngineTest {

    @Test
    fun detect_hindiDevanagari_identifiesHindiAndDevanagariScript() {
        val claim = "सरकार कॉलेज के छात्रों को मुफ्त लैपटॉप बांट रही है"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.HINDI, metadata.language)
        assertEquals("Devanagari", metadata.detectedScript)
        assertFalse(metadata.isTransliterated)
    }

    @Test
    fun detect_marathiDevanagari_identifiesMarathi() {
        val claim = "शासनाने मोफत लॅपटॉप वाटप योजना जाहीर केली आहे"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.MARATHI, metadata.language)
        assertEquals("Devanagari", metadata.detectedScript)
    }

    @Test
    fun detect_telugu_identifiesTeluguScript() {
        val claim = "ప్రభుత్వం కళాశాల విద్యార్థులకు ఉచిత ల్యాప్‌టాప్‌లను పంపిణీ చేస్తోంది"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.TELUGU, metadata.language)
        assertEquals("Telugu", metadata.detectedScript)
    }

    @Test
    fun detect_tamil_identifiesTamilScript() {
        val claim = "கல்லூரி மாணவர்களுக்கு அரசு இலவச லேப்டாப் வழங்குகிறது"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.TAMIL, metadata.language)
        assertEquals("Tamil", metadata.detectedScript)
    }

    @Test
    fun detect_kannada_identifiesKannadaScript() {
        val claim = "ಸರ್ಕಾರ ಕಾಲೇಜು ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ಉಚಿತ ಲ್ಯಾಪ್‌ಟಾಪ್ ವಿತರಿಸುತ್ತಿದೆ"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.KANNADA, metadata.language)
        assertEquals("Kannada", metadata.detectedScript)
    }

    @Test
    fun detect_malayalam_identifiesMalayalamScript() {
        val claim = "കോളേജ് വിദ്യാർത്ഥികൾക്ക് സർക്കാർ സൗജന്യ ലാപ്ടോപ്പ് നൽകുന്നു"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.MALAYALAM, metadata.language)
        assertEquals("Malayalam", metadata.detectedScript)
    }

    @Test
    fun detect_bengali_identifiesBengaliScript() {
        val claim = "সরকার কলেজ ছাত্রদের বিনামূল্যে ল্যাপটপ দিচ্ছে"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.BENGALI, metadata.language)
        assertEquals("Bengali", metadata.detectedScript)
    }

    @Test
    fun detect_english_identifiesEnglishLatinScript() {
        val claim = "Government is distributing free laptops to college students"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.ENGLISH, metadata.language)
        assertEquals("Latin", metadata.detectedScript)
        assertFalse(metadata.isTransliterated)
    }

    @Test
    fun detect_hinglishTransliterated_identifiesHinglishWithFlag() {
        val claim = "Sarkar sabhi chhatro ko muft laptop de rahi hai jaldi aavedan karo"
        val metadata = LanguageDetectionEngine.detect(claim)

        assertEquals(SupportedIndianLanguage.HINGLISH_TRANSLITERATED, metadata.language)
        assertEquals("Latin", metadata.detectedScript)
        assertTrue(metadata.isTransliterated)
    }

    @Test
    fun multilingualClaimService_preservesOriginalTextAndExtractsUrls() = runBlocking {
        val service = DeterministicMultilingualClaimService()
        val text = "Check this link https://pib.gov.in/test for free laptop yojana"
        val result = service.processClaim(text)

        assertTrue(result.isSuccess)
        val claim = result.getOrThrow()
        assertEquals(text, claim.originalClaim)
        assertEquals(listOf("https://pib.gov.in/test"), claim.preservedEntities)
    }
}
