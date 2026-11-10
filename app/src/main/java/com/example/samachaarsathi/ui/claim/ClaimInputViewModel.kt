package com.example.samachaarsathi.ui.claim

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.samachaarsathi.data.ClaimUnderstandingRepository
import com.example.samachaarsathi.data.DeterministicEvidenceAnalysisRepository
import com.example.samachaarsathi.data.DeterministicVerdictRepository
import com.example.samachaarsathi.data.EvidenceAnalysisRepository
import com.example.samachaarsathi.data.EvidenceRetrievalRepository
import com.example.samachaarsathi.data.FirebaseClaimUnderstandingRepository
import com.example.samachaarsathi.data.FirebaseEvidenceRetrievalRepository
import com.example.samachaarsathi.data.FirebaseGeminiRepository
import com.example.samachaarsathi.data.GeminiRepository
import com.example.samachaarsathi.data.VerdictRepository
import com.example.samachaarsathi.data.image.ImageOcrService
import com.example.samachaarsathi.data.image.MlKitImageOcrService
import com.example.samachaarsathi.data.local.VerificationHistoryRepository
import com.example.samachaarsathi.data.model.ClaimUnderstanding
import com.example.samachaarsathi.data.model.EvidenceCollection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClaimInputViewModel(
    private val geminiRepository: GeminiRepository = FirebaseGeminiRepository(),
    private val claimUnderstandingRepository: ClaimUnderstandingRepository = FirebaseClaimUnderstandingRepository(),
    private val evidenceRetrievalRepository: EvidenceRetrievalRepository = FirebaseEvidenceRetrievalRepository(),
    private val evidenceAnalysisRepository: EvidenceAnalysisRepository = DeterministicEvidenceAnalysisRepository(),
    private val verdictRepository: VerdictRepository = DeterministicVerdictRepository(),
    private val imageOcrService: ImageOcrService = MlKitImageOcrService(),
    private val historyRepository: VerificationHistoryRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClaimInputUiState())
    val uiState: StateFlow<ClaimInputUiState> = _uiState.asStateFlow()

    init {
        historyRepository?.let { repo ->
            viewModelScope.launch {
                repo.getAllHistory().collect { persistentItems ->
                    _uiState.update { currentState ->
                        if (persistentItems.isNotEmpty() || currentState.sessionHistory.isEmpty()) {
                            currentState.copy(sessionHistory = persistentItems)
                        } else {
                            currentState
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the current claim text as the user types or pastes content.
     */
    fun onClaimTextChanged(newText: String) {
        _uiState.update { currentState ->
            currentState.copy(
                claimText = newText,
                // Clear any prior confirmation message when user edits text
                confirmationMessage = null,
                // Reset previous assessment and error states when user modifies the claim
                analysisStatus = AnalysisStatus.Idle,
                understandingStatus = UnderstandingStatus.Idle,
                evidenceStatus = EvidenceStatus.NotRetrieved,
                verdictStatus = VerdictStatus.Idle,
            )
        }
    }

    /**
     * Validates and captures the claim, initiates Claim Understanding Engine extraction,
     * and streams response chunks for preliminary assessment.
     */
    fun onVerifyClaimClicked() {
        val trimmedClaim = _uiState.value.claimText.trim()
        if (trimmedClaim.isBlank() || _uiState.value.isLoading) {
            return
        }

        val historyItem = SessionVerificationItem(
            id = "claim_${System.currentTimeMillis()}",
            claimText = trimmedClaim,
            timestampMs = System.currentTimeMillis(),
        )

        _uiState.update { currentState ->
            val existing = currentState.sessionHistory.filterNot { it.claimText.equals(trimmedClaim, ignoreCase = true) }
            currentState.copy(
                claimText = trimmedClaim,
                lastSubmittedClaim = trimmedClaim,
                confirmationMessage = "Claim captured.",
                analysisStatus = AnalysisStatus.Loading(),
                understandingStatus = UnderstandingStatus.Loading,
                evidenceStatus = EvidenceStatus.NotRetrieved,
                verdictStatus = VerdictStatus.Idle,
                sessionHistory = listOf(historyItem) + existing,
            )
        }

        viewModelScope.launch {
            historyRepository?.saveVerification(historyItem)
        }

        // Launch Claim Understanding Engine (Brick 3)
        viewModelScope.launch {
            Log.d(TAG, "Initiating Claim Understanding Engine for: \"${trimmedClaim.take(40)}...\"")
            val result = claimUnderstandingRepository.understandClaim(trimmedClaim)
            result.fold(
                onSuccess = { understanding ->
                    Log.d(TAG, "Claim understanding succeeded: coreClaim=\"${understanding.coreClaim}\"")
                    _uiState.update { currentState ->
                        currentState.copy(
                            understandingStatus = UnderstandingStatus.Success(understanding),
                        )
                    }
                },
                onFailure = { throwable ->
                    if (throwable is CancellationException) {
                        throw throwable
                    }
                    Log.e(TAG, "Claim understanding failed", throwable)
                    val errorMsg = throwable.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Failed to extract structured understanding."
                    _uiState.update { currentState ->
                        currentState.copy(
                            understandingStatus = UnderstandingStatus.Error(errorMsg),
                        )
                    }
                },
            )
        }

        // Launch Preliminary Assessment streaming (Brick 2 / 2.1)
        viewModelScope.launch {
            Log.d(TAG, "Initiating streaming Gemini claim analysis for claim: \"${trimmedClaim.take(40)}...\"")
            val accumulatedResponse = StringBuilder()
            try {
                geminiRepository.analyzeClaimStream(trimmedClaim).collect { chunk ->
                    accumulatedResponse.append(chunk)
                    _uiState.update { currentState ->
                        currentState.copy(
                            analysisStatus = AnalysisStatus.Loading(partialResponse = accumulatedResponse.toString()),
                        )
                    }
                }

                val finalResponse = accumulatedResponse.toString().trim()
                if (finalResponse.isBlank()) {
                    Log.w(TAG, "Gemini stream completed with empty response")
                    _uiState.update { currentState ->
                        currentState.copy(
                            analysisStatus = AnalysisStatus.Error("No response text returned by the model"),
                        )
                    }
                } else {
                    Log.d(TAG, "Gemini stream completed successfully (${finalResponse.length} chars)")
                    _uiState.update { currentState ->
                        currentState.copy(
                            analysisStatus = AnalysisStatus.Success(finalResponse),
                        )
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) {
                    throw throwable
                }
                Log.e(TAG, "Gemini streaming claim analysis failed", throwable)
                val partialText = accumulatedResponse.toString().trim()
                val rawErrorMessage = throwable.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Failed to analyze claim. Please try again."

                val errorMessage = if (partialText.isNotBlank()) {
                    "Analysis interrupted: $rawErrorMessage"
                } else {
                    rawErrorMessage
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        analysisStatus = AnalysisStatus.Error(
                            message = errorMessage,
                            partialResponse = partialText,
                        ),
                    )
                }
            }
        }
    }

    /**
     * Initiates Google Search Grounding evidence retrieval for the current claim and its structured assertions.
     */
    fun onRetrieveEvidenceClicked() {
        val currentClaim = _uiState.value.lastSubmittedClaim ?: _uiState.value.claimText.trim()
        if (currentClaim.isBlank() || _uiState.value.evidenceStatus is EvidenceStatus.Loading) {
            return
        }

        val understanding = when (val u = _uiState.value.understandingStatus) {
            is UnderstandingStatus.Success -> u.understanding
            else -> ClaimUnderstanding(
                originalClaim = currentClaim,
                coreClaim = currentClaim,
                claimType = "other",
                keyAssertions = listOf(currentClaim),
            )
        }

        _uiState.update { it.copy(evidenceStatus = EvidenceStatus.Loading) }

        viewModelScope.launch {
            Log.d(TAG, "Initiating Google Search Grounding evidence retrieval for: \"${currentClaim.take(40)}...\"")
            val result = evidenceRetrievalRepository.retrieveEvidenceForUnderstanding(understanding)
            result.fold(
                onSuccess = { evidenceCollection ->
                    Log.d(TAG, "Evidence retrieval succeeded with ${evidenceCollection.allSources.size} sources")
                    _uiState.update { it.copy(evidenceStatus = EvidenceStatus.Success(evidenceCollection)) }
                    synthesizeVerdict(understanding, evidenceCollection)
                },
                onFailure = { throwable ->
                    if (throwable is CancellationException) {
                        throw throwable
                    }
                    Log.e(TAG, "Evidence retrieval failed", throwable)
                    val errorMsg = throwable.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Failed to retrieve web evidence."
                    _uiState.update {
                        it.copy(
                            evidenceStatus = EvidenceStatus.Error(errorMsg),
                            verdictStatus = VerdictStatus.Error("Cannot generate verdict due to evidence retrieval failure: $errorMsg"),
                        )
                    }
                },
            )
        }
    }

    /**
     * Explicit trigger for evidence analysis & verdict determination.
     */
    fun onSynthesizeVerdictClicked() {
        val evidenceCollection = when (val ev = _uiState.value.evidenceStatus) {
            is EvidenceStatus.Success -> ev.evidence
            else -> return
        }
        val currentClaim = _uiState.value.lastSubmittedClaim ?: _uiState.value.claimText.trim()
        val understanding = when (val u = _uiState.value.understandingStatus) {
            is UnderstandingStatus.Success -> u.understanding
            else -> ClaimUnderstanding(
                originalClaim = currentClaim,
                coreClaim = currentClaim,
                claimType = "other",
                keyAssertions = listOf(currentClaim),
            )
        }
        synthesizeVerdict(understanding, evidenceCollection)
    }

    private fun synthesizeVerdict(
        understanding: ClaimUnderstanding,
        evidence: EvidenceCollection,
    ) {
        _uiState.update { it.copy(verdictStatus = VerdictStatus.Loading) }
        viewModelScope.launch {
            Log.d(TAG, "Initiating evidence analysis & verdict synthesis...")
            val analysisResult = evidenceAnalysisRepository.analyzeEvidence(understanding, evidence)
            analysisResult.fold(
                onSuccess = { analysis ->
                    val verdictResult = verdictRepository.determineVerdict(understanding, evidence, analysis)
                    verdictResult.fold(
                        onSuccess = { verdict ->
                            Log.d(TAG, "Verdict synthesized: ${verdict.category} (headline=\"${verdict.headline}\")")
                            _uiState.update { state ->
                                var matched = false
                                val updatedHistory = state.sessionHistory.map { item ->
                                    if (item.claimText.equals(understanding.originalClaim, ignoreCase = true) ||
                                        item.claimText.equals(understanding.coreClaim, ignoreCase = true)
                                    ) {
                                        matched = true
                                        val withVerdict = item.copy(verdict = verdict)
                                        viewModelScope.launch {
                                            historyRepository?.saveVerification(withVerdict)
                                        }
                                        withVerdict
                                    } else {
                                        item
                                    }
                                }
                                val finalHistory = if (!matched) {
                                    val newItem = SessionVerificationItem(
                                        id = "claim_${verdict.timestamp}",
                                        claimText = understanding.originalClaim,
                                        timestampMs = verdict.timestamp,
                                        verdict = verdict,
                                    )
                                    viewModelScope.launch {
                                        historyRepository?.saveVerification(newItem)
                                    }
                                    listOf(newItem) + updatedHistory
                                } else {
                                    updatedHistory
                                }
                                state.copy(
                                    verdictStatus = VerdictStatus.Success(verdict),
                                    sessionHistory = finalHistory,
                                )
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Verdict determination failed", error)
                            _uiState.update {
                                it.copy(verdictStatus = VerdictStatus.Error(error.localizedMessage ?: "Failed to determine verdict."))
                            }
                        },
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Evidence analysis failed", error)
                    _uiState.update {
                        it.copy(verdictStatus = VerdictStatus.Error(error.localizedMessage ?: "Failed to analyze evidence."))
                    }
                },
            )
        }
    }

    /**
     * Handles local image / screenshot selection and triggers on-device OCR.
     */
    fun onImageSelected(context: Context, uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, isOcrLoading = true) }
        viewModelScope.launch {
            val result = imageOcrService.processImageUri(context, uri)
            result.fold(
                onSuccess = { ocrResult ->
                    _uiState.update { it.copy(ocrResult = ocrResult, isOcrLoading = false) }
                },
                onFailure = { error ->
                    Log.e(TAG, "OCR extraction failed", error)
                    _uiState.update { it.copy(isOcrLoading = false) }
                },
            )
        }
    }

    /**
     * Clears attached image and OCR results.
     */
    fun onImageRemoved() {
        _uiState.update { it.copy(selectedImageUri = null, ocrResult = null, isOcrLoading = false) }
    }

    /**
     * Adopts the extracted/edited text as the active claim input.
     */
    fun onUseExtractedText(text: String) {
        onClaimTextChanged(text)
    }

    /**
     * Toggles bookmark / saved status of an entry in session history.
     */
    fun onToggleSaveClaim(claimId: String) {
        _uiState.update { state ->
            val updated = state.sessionHistory.map { item ->
                if (item.id == claimId) {
                    val newSaved = !item.isSaved
                    viewModelScope.launch {
                        historyRepository?.setSaved(claimId, newSaved)
                    }
                    item.copy(isSaved = newSaved)
                } else {
                    item
                }
            }
            state.copy(sessionHistory = updated)
        }
    }

    /**
     * Explicitly removes an item from Saved without removing it from history.
     */
    fun onRemoveSavedClaim(claimId: String) {
        _uiState.update { state ->
            val updated = state.sessionHistory.map { item ->
                if (item.id == claimId) {
                    viewModelScope.launch {
                        historyRepository?.setSaved(claimId, false)
                    }
                    item.copy(isSaved = false)
                } else {
                    item
                }
            }
            state.copy(sessionHistory = updated)
        }
    }

    /**
     * Selects an entry from history or saved items and loads it into the active verification state.
     */
    fun onSelectHistoryItem(item: SessionVerificationItem) {
        _uiState.update { state ->
            val verdict = item.verdict
            if (verdict != null) {
                state.copy(
                    claimText = item.claimText,
                    lastSubmittedClaim = item.claimText,
                    confirmationMessage = null,
                    understandingStatus = UnderstandingStatus.Success(verdict.claimUnderstanding),
                    evidenceStatus = EvidenceStatus.Success(verdict.evidenceAnalysis.evidenceCollection),
                    verdictStatus = VerdictStatus.Success(verdict),
                    analysisStatus = AnalysisStatus.Success(verdict.reasoning.primaryExplanation),
                )
            } else {
                state.copy(
                    claimText = item.claimText,
                    lastSubmittedClaim = item.claimText,
                    confirmationMessage = null,
                    analysisStatus = AnalysisStatus.Idle,
                    understandingStatus = UnderstandingStatus.Idle,
                    evidenceStatus = EvidenceStatus.NotRetrieved,
                    verdictStatus = VerdictStatus.Idle,
                )
            }
        }
    }

    /**
     * Seeds demo session history for offline UI preview, QA, and screenshot capture.
     * ZERO LIVE API CALLS are made.
     */
    fun populateDemoSessionHistory(items: List<SessionVerificationItem>, persistToDatabase: Boolean = false) {
        _uiState.update { state ->
            state.copy(sessionHistory = items)
        }
        if (persistToDatabase) {
            viewModelScope.launch {
                historyRepository?.saveVerifications(items)
            }
        }
    }

    /**
     * Seeds demo active progress state for offline UI review, QA, and screenshot capture.
     * ZERO LIVE API CALLS are made.
     */
    fun populateDemoProgressState(claim: String) {
        _uiState.update { state ->
            state.copy(
                claimText = claim,
                lastSubmittedClaim = claim,
                understandingStatus = UnderstandingStatus.Success(
                    com.example.samachaarsathi.data.model.DemoFixtures.sampleUnderstanding
                ),
                evidenceStatus = EvidenceStatus.Loading,
                verdictStatus = VerdictStatus.Idle,
                analysisStatus = AnalysisStatus.Idle,
            )
        }
    }

    /**
     * Seeds demo OCR extraction state for offline UI review, QA, and screenshot capture.
     * ZERO LIVE API CALLS are made.
     */
    fun populateDemoOcrState() {
        _uiState.update { state ->
            val ocr = com.example.samachaarsathi.data.model.DemoFixtures.sampleOcrResult
            state.copy(
                selectedImageUri = ocr.imageUri,
                ocrResult = ocr,
                isOcrLoading = false,
                claimText = "",
            )
        }
    }

    /**
     * Seeds demo verdict state for offline UI review, QA, and screenshot capture.
     * ZERO LIVE API CALLS are made.
     */
    fun populateDemoVerdict(verdict: com.example.samachaarsathi.data.model.VerificationVerdict) {
        _uiState.update { state ->
            state.copy(
                claimText = verdict.claimUnderstanding.originalClaim,
                lastSubmittedClaim = verdict.claimUnderstanding.originalClaim,
                understandingStatus = UnderstandingStatus.Success(verdict.claimUnderstanding),
                evidenceStatus = EvidenceStatus.Success(verdict.evidenceAnalysis.evidenceCollection),
                verdictStatus = VerdictStatus.Success(verdict),
                analysisStatus = AnalysisStatus.Success(verdict.reasoning.primaryExplanation),
            )
        }
    }

    companion object {
        private const val TAG = "ClaimInputViewModel"

        fun provideFactory(
            historyRepository: VerificationHistoryRepository,
            geminiRepository: GeminiRepository = FirebaseGeminiRepository(),
            claimUnderstandingRepository: ClaimUnderstandingRepository = FirebaseClaimUnderstandingRepository(),
            evidenceRetrievalRepository: EvidenceRetrievalRepository = FirebaseEvidenceRetrievalRepository(),
            evidenceAnalysisRepository: EvidenceAnalysisRepository = DeterministicEvidenceAnalysisRepository(),
            verdictRepository: VerdictRepository = DeterministicVerdictRepository(),
            imageOcrService: ImageOcrService = MlKitImageOcrService(),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ClaimInputViewModel(
                    geminiRepository = geminiRepository,
                    claimUnderstandingRepository = claimUnderstandingRepository,
                    evidenceRetrievalRepository = evidenceRetrievalRepository,
                    evidenceAnalysisRepository = evidenceAnalysisRepository,
                    verdictRepository = verdictRepository,
                    imageOcrService = imageOcrService,
                    historyRepository = historyRepository,
                ) as T
            }
        }
    }
}
