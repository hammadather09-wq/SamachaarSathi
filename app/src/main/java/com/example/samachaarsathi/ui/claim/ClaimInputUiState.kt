package com.example.samachaarsathi.ui.claim

import android.net.Uri
import com.example.samachaarsathi.data.image.OcrExtractionResult
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceCollection
import com.example.samachaarsathi.data.model.VerificationVerdict

/**
 * Represents the status of the claim verification / analysis request.
 */
sealed interface AnalysisStatus {
    data object Idle : AnalysisStatus
    data class Loading(val partialResponse: String = "") : AnalysisStatus
    data class Success(val response: String) : AnalysisStatus
    data class Error(val message: String, val partialResponse: String = "") : AnalysisStatus
}

/**
 * Represents the status of the structured claim understanding request (Brick 3).
 */
sealed interface UnderstandingStatus {
    data object Idle : UnderstandingStatus
    data object Loading : UnderstandingStatus
    data class Success(val understanding: ClaimUnderstanding) : UnderstandingStatus
    data class Error(val message: String) : UnderstandingStatus
}

/**
 * Represents the status of the Google Search Grounding evidence retrieval request (Brick 4).
 */
sealed interface EvidenceStatus {
    data object NotRetrieved : EvidenceStatus
    data object Loading : EvidenceStatus
    data class Success(val evidence: EvidenceCollection) : EvidenceStatus
    data class Error(val message: String) : EvidenceStatus
}

/**
 * Represents the status of the verdict synthesis request (Bricks 5-7).
 */
sealed interface VerdictStatus {
    data object Idle : VerdictStatus
    data object Loading : VerdictStatus
    data class Success(val verdict: VerificationVerdict) : VerdictStatus
    data class Error(val message: String) : VerdictStatus
}

/**
 * Verification entry tracked during the active app session.
 */
data class SessionVerificationItem(
    val id: String,
    val claimText: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val verdict: VerificationVerdict? = null,
    val isSaved: Boolean = false,
)

/**
 * State representing the Claim Input screen.
 *
 * @property claimText Current text entered by the user in the claim input field.
 * @property lastSubmittedClaim The trimmed claim text that was captured on submission.
 * @property confirmationMessage Simple confirmation text shown after a claim is captured.
 * @property analysisStatus Current status of the Gemini analysis request.
 * @property understandingStatus Current status of the structured claim understanding request.
 * @property evidenceStatus Current status of the Google Search Grounding evidence retrieval request.
 * @property verdictStatus Current status of the complete verification verdict.
 * @property selectedImageUri URI of the attached image or screenshot.
 * @property ocrResult On-device OCR extraction and forwarding metadata.
 * @property isOcrLoading True while local text recognition is running.
 * @property sessionHistory Session-level verification items for the Library screen.
 */
data class ClaimInputUiState(
    val claimText: String = "",
    val lastSubmittedClaim: String? = null,
    val confirmationMessage: String? = null,
    val analysisStatus: AnalysisStatus = AnalysisStatus.Idle,
    val understandingStatus: UnderstandingStatus = UnderstandingStatus.Idle,
    val evidenceStatus: EvidenceStatus = EvidenceStatus.NotRetrieved,
    val verdictStatus: VerdictStatus = VerdictStatus.Idle,
    val selectedImageUri: Uri? = null,
    val ocrResult: OcrExtractionResult? = null,
    val isOcrLoading: Boolean = false,
    val sessionHistory: List<SessionVerificationItem> = emptyList(),
) {
    /**
     * Determines whether the "Verify Claim" button should be enabled.
     * Must be disabled when the input is empty or only whitespace, or while a request is in progress.
     */
    val canSubmit: Boolean
        get() = claimText.trim().isNotBlank() &&
            (analysisStatus !is AnalysisStatus.Loading) &&
            (understandingStatus !is UnderstandingStatus.Loading) &&
            (evidenceStatus !is EvidenceStatus.Loading) &&
            (verdictStatus !is VerdictStatus.Loading) &&
            !isOcrLoading

    val isLoading: Boolean
        get() = (analysisStatus is AnalysisStatus.Loading) ||
            (understandingStatus is UnderstandingStatus.Loading) ||
            (evidenceStatus is EvidenceStatus.Loading) ||
            (verdictStatus is VerdictStatus.Loading) ||
            isOcrLoading
}
