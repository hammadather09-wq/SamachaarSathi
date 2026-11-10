package com.example.samachaarsathi.ui.claim

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import com.example.samachaarsathi.data.GeminiRepository
import com.example.samachaarsathi.data.image.ForwardingMetadata
import com.example.samachaarsathi.data.image.ImageOcrService
import com.example.samachaarsathi.data.image.ImageValidationResult
import com.example.samachaarsathi.data.image.OcrExtractionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClaimInputViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeGeminiRepository
    private lateinit var viewModel: ClaimInputViewModel

    class FakeGeminiRepository : GeminiRepository {
        var chunksToEmit: List<String> = listOf("Preliminary ", "AI ", "assessment")
        var errorToThrow: Throwable? = null
        var lastAnalyzedClaim: String? = null
        var callCount: Int = 0

        override fun analyzeClaimStream(claim: String): Flow<String> = flow {
            callCount++
            lastAnalyzedClaim = claim
            for (chunk in chunksToEmit) {
                emit(chunk)
            }
            errorToThrow?.let { throw it }
        }

        override suspend fun analyzeClaim(claim: String): Result<String> {
            return try {
                val full = StringBuilder()
                analyzeClaimStream(claim).collect { full.append(it) }
                Result.success(full.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    class FakeClaimUnderstandingRepository : com.example.samachaarsathi.data.ClaimUnderstandingRepository {
        var resultToReturn: Result<com.example.samachaarsathi.data.model.ClaimUnderstanding>? = null
        var lastAnalyzedClaim: String? = null
        var callCount: Int = 0

        override suspend fun understandClaim(claim: String): Result<com.example.samachaarsathi.data.model.ClaimUnderstanding> {
            callCount++
            lastAnalyzedClaim = claim
            return resultToReturn ?: Result.success(
                com.example.samachaarsathi.data.model.ClaimUnderstanding(
                    originalClaim = claim,
                    coreClaim = claim.trim(),
                    claimType = "government announcement",
                    keyAssertions = listOf("Assertion 1: $claim"),
                )
            )
        }
    }

    class FakeEvidenceRetrievalRepository : com.example.samachaarsathi.data.EvidenceRetrievalRepository {
        var resultToReturn: Result<com.example.samachaarsathi.data.model.EvidenceCollection>? = null
        var lastUnderstanding: com.example.samachaarsathi.data.model.ClaimUnderstanding? = null
        var callCount: Int = 0

        override suspend fun retrieveEvidenceForUnderstanding(
            understanding: com.example.samachaarsathi.data.model.ClaimUnderstanding
        ): Result<com.example.samachaarsathi.data.model.EvidenceCollection> {
            callCount++
            lastUnderstanding = understanding
            return resultToReturn ?: Result.success(
                com.example.samachaarsathi.data.model.EvidenceCollection(
                    originalClaim = understanding.originalClaim,
                    structuredClaim = understanding,
                    assertionEvidence = listOf(
                        com.example.samachaarsathi.data.model.AssertionEvidence(
                            assertionId = "assertion_1",
                            assertionText = understanding.coreClaim,
                            sources = listOf(
                                com.example.samachaarsathi.data.model.EvidenceSource(
                                    id = "src_1",
                                    title = "PIB Official Notification",
                                    url = "https://pib.gov.in/test",
                                    domain = "pib.gov.in",
                                    snippet = "Official notification launched scheme for students.",
                                )
                            ),
                            retrievalStatus = com.example.samachaarsathi.data.model.RetrievalStatus.SUCCESS,
                        )
                    ),
                    overallRetrievalStatus = com.example.samachaarsathi.data.model.RetrievalStatus.SUCCESS,
                )
            )
        }

        override suspend fun retrieveEvidenceForAssertion(
            assertionId: String,
            assertionText: String
        ): Result<com.example.samachaarsathi.data.model.AssertionEvidence> {
            return Result.success(
                com.example.samachaarsathi.data.model.AssertionEvidence(
                    assertionId = assertionId,
                    assertionText = assertionText,
                    retrievalStatus = com.example.samachaarsathi.data.model.RetrievalStatus.SUCCESS,
                )
            )
        }
    }

    class FakeImageOcrService : ImageOcrService {
        var resultToReturn: Result<OcrExtractionResult>? = null
        var lastUri: Uri? = null

        override fun validateImage(context: Context, uri: Uri): ImageValidationResult {
            return ImageValidationResult(isValid = true)
        }

        override suspend fun processImageUri(context: Context, uri: Uri): Result<OcrExtractionResult> {
            lastUri = uri
            return resultToReturn ?: Result.success(
                OcrExtractionResult(
                    rawExtractedText = "Forwarded\nFake OCR Extracted Text",
                    cleanedClaimText = "Fake OCR Extracted Text",
                    forwardingMetadata = ForwardingMetadata(
                        isForwarded = true,
                        headerMarker = "Forwarded",
                    ),
                    hasReadableText = true,
                    imageUri = uri,
                )
            )
        }

        override suspend fun processBitmap(bitmap: Bitmap, uri: Uri?): Result<OcrExtractionResult> {
            return Result.success(
                OcrExtractionResult(
                    rawExtractedText = "Raw",
                    cleanedClaimText = "Clean",
                    hasReadableText = true,
                    imageUri = uri,
                )
            )
        }
    }

    private lateinit var fakeUnderstandingRepository: FakeClaimUnderstandingRepository
    private lateinit var fakeEvidenceRepository: FakeEvidenceRetrievalRepository
    private lateinit var fakeImageOcrService: FakeImageOcrService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeGeminiRepository()
        fakeUnderstandingRepository = FakeClaimUnderstandingRepository()
        fakeEvidenceRepository = FakeEvidenceRetrievalRepository()
        fakeImageOcrService = FakeImageOcrService()
        viewModel = ClaimInputViewModel(
            geminiRepository = fakeRepository,
            claimUnderstandingRepository = fakeUnderstandingRepository,
            evidenceRetrievalRepository = fakeEvidenceRepository,
            evidenceAnalysisRepository = com.example.samachaarsathi.data.DeterministicEvidenceAnalysisRepository(testDispatcher),
            verdictRepository = com.example.samachaarsathi.data.DeterministicVerdictRepository(testDispatcher),
            imageOcrService = fakeImageOcrService,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyInputAndCannotSubmit() {
        val state = viewModel.uiState.value
        assertEquals("", state.claimText)
        assertNull(state.lastSubmittedClaim)
        assertNull(state.confirmationMessage)
        assertFalse(state.canSubmit)
        assertTrue(state.analysisStatus is AnalysisStatus.Idle)
    }

    @Test
    fun emptyInput_cannotSubmit() {
        viewModel.onClaimTextChanged("")
        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)

        viewModel.onVerifyClaimClicked()
        assertNull(viewModel.uiState.value.lastSubmittedClaim)
        assertNull(viewModel.uiState.value.confirmationMessage)
        assertEquals(0, fakeRepository.callCount)
    }

    @Test
    fun whitespaceOnlyInput_cannotSubmit() {
        viewModel.onClaimTextChanged("    \n\t  ")
        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)

        viewModel.onVerifyClaimClicked()
        assertNull(viewModel.uiState.value.lastSubmittedClaim)
        assertNull(viewModel.uiState.value.confirmationMessage)
        assertEquals(0, fakeRepository.callCount)
    }

    @Test
    fun normalClaim_submitsToGeminiAndSucceeds() = runTest(testDispatcher) {
        val claim = "  Drinking hot water cures viral fever immediately.  "
        viewModel.onClaimTextChanged(claim)
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.onVerifyClaimClicked()

        // Immediate state before coroutine completes
        val loadingState = viewModel.uiState.value
        assertEquals("Drinking hot water cures viral fever immediately.", loadingState.claimText)
        assertEquals("Drinking hot water cures viral fever immediately.", loadingState.lastSubmittedClaim)
        assertEquals("Claim captured.", loadingState.confirmationMessage)
        assertTrue(loadingState.analysisStatus is AnalysisStatus.Loading)
        assertFalse(loadingState.canSubmit)

        advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertEquals(1, fakeRepository.callCount)
        assertEquals("Drinking hot water cures viral fever immediately.", fakeRepository.lastAnalyzedClaim)
        assertTrue(successState.analysisStatus is AnalysisStatus.Success)
        val response = (successState.analysisStatus as AnalysisStatus.Success).response
        assertEquals("Preliminary AI assessment", response)
        assertTrue(successState.canSubmit)
    }

    @Test
    fun multilinePastedText_preservesContentAndCapturesTrimmed() = runTest(testDispatcher) {
        val multilineClaim = """
            
            Forwarded as received:
            Breaking news from WHO about emergency lockdowns across the country!
            Please share with family and friends.
            
        """.trimIndent()

        viewModel.onClaimTextChanged(multilineClaim)
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(multilineClaim.trim(), state.claimText)
        assertEquals(multilineClaim.trim(), state.lastSubmittedClaim)
        assertEquals(multilineClaim.trim(), fakeRepository.lastAnalyzedClaim)
        assertTrue(state.analysisStatus is AnalysisStatus.Success)
    }

    @Test
    fun geminiFailure_updatesStateToError() = runTest(testDispatcher) {
        fakeRepository.chunksToEmit = emptyList()
        fakeRepository.errorToThrow = RuntimeException("Network timeout")

        viewModel.onClaimTextChanged("Any claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.analysisStatus is AnalysisStatus.Error)
        val errorMessage = (state.analysisStatus as AnalysisStatus.Error).message
        assertEquals("Network timeout", errorMessage)
        assertTrue(state.canSubmit)
    }

    @Test
    fun geminiInterrupted_updatesStateToInterruptedErrorWithPartialResponse() = runTest(testDispatcher) {
        fakeRepository.chunksToEmit = listOf("Partial ", "analysis ")
        fakeRepository.errorToThrow = RuntimeException("Connection reset")

        viewModel.onClaimTextChanged("Any claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.analysisStatus is AnalysisStatus.Error)
        val errorStatus = state.analysisStatus as AnalysisStatus.Error
        assertEquals("Analysis interrupted: Connection reset", errorStatus.message)
        assertEquals("Partial analysis", errorStatus.partialResponse)
        assertTrue(state.canSubmit)
    }

    @Test
    fun geminiEmptyResponse_updatesStateToError() = runTest(testDispatcher) {
        fakeRepository.chunksToEmit = emptyList()
        fakeRepository.errorToThrow = null

        viewModel.onClaimTextChanged("Any claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.analysisStatus is AnalysisStatus.Error)
        val errorMessage = (state.analysisStatus as AnalysisStatus.Error).message
        assertEquals("No response text returned by the model", errorMessage)
        assertTrue(state.canSubmit)
    }

    @Test
    fun editingTextAfterSubmission_clearsConfirmationMessageAndResetsAnalysisStatus() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Valid claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()
        assertEquals("Claim captured.", viewModel.uiState.value.confirmationMessage)
        assertTrue(viewModel.uiState.value.analysisStatus is AnalysisStatus.Success)

        viewModel.onClaimTextChanged("Valid claim edited")
        assertNull(viewModel.uiState.value.confirmationMessage)
        assertTrue(viewModel.uiState.value.analysisStatus is AnalysisStatus.Idle)
        assertEquals("Valid claim", viewModel.uiState.value.lastSubmittedClaim)
    }

    @Test
    fun editingTextAfterSuccess_resetsAnalysisStatusToIdleAndClearsPreviousAssessment() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Valid claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.analysisStatus is AnalysisStatus.Success)
        val successResponse = (viewModel.uiState.value.analysisStatus as AnalysisStatus.Success).response
        assertEquals("Preliminary AI assessment", successResponse)

        // User edits the claim
        viewModel.onClaimTextChanged("Valid claim with additional detail")

        val state = viewModel.uiState.value
        assertEquals("Valid claim with additional detail", state.claimText)
        assertTrue(state.analysisStatus is AnalysisStatus.Idle)
        assertNull(state.confirmationMessage)
        assertTrue(state.canSubmit)
    }

    @Test
    fun editingTextAfterError_resetsAnalysisStatusToIdle() = runTest(testDispatcher) {
        fakeRepository.chunksToEmit = emptyList()
        fakeRepository.errorToThrow = RuntimeException("Network error")

        viewModel.onClaimTextChanged("Failing claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.analysisStatus is AnalysisStatus.Error)

        viewModel.onClaimTextChanged("New claim attempt")
        assertTrue(viewModel.uiState.value.analysisStatus is AnalysisStatus.Idle)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun normalClaim_triggersClaimUnderstandingAndSucceeds() = runTest(testDispatcher) {
        val claim = "Government announced free laptops for college students"
        viewModel.onClaimTextChanged(claim)
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        assertEquals(1, fakeUnderstandingRepository.callCount)
        assertEquals(claim, fakeUnderstandingRepository.lastAnalyzedClaim)
        assertTrue(viewModel.uiState.value.understandingStatus is UnderstandingStatus.Success)
        val understanding = (viewModel.uiState.value.understandingStatus as UnderstandingStatus.Success).understanding
        assertEquals(claim, understanding.coreClaim)
        assertEquals("government announcement", understanding.claimType)
    }

    @Test
    fun claimUnderstandingFailure_updatesUnderstandingStatusToError() = runTest(testDispatcher) {
        fakeUnderstandingRepository.resultToReturn = Result.failure(RuntimeException("Parsing error"))

        viewModel.onClaimTextChanged("Any claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.understandingStatus is UnderstandingStatus.Error)
        val errorMsg = (viewModel.uiState.value.understandingStatus as UnderstandingStatus.Error).message
        assertEquals("Parsing error", errorMsg)
    }

    @Test
    fun editingTextAfterClaimUnderstanding_resetsUnderstandingStatusToIdle() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Valid claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.understandingStatus is UnderstandingStatus.Success)

        viewModel.onClaimTextChanged("Edited claim")
        assertTrue(viewModel.uiState.value.understandingStatus is UnderstandingStatus.Idle)
    }

    @Test
    fun initialState_hasNotRetrievedEvidenceStatus() {
        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.NotRetrieved)
    }

    @Test
    fun retrieveEvidence_transitionsToLoadingThenSuccess() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Government distributes free laptop")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        // Trigger evidence retrieval
        viewModel.onRetrieveEvidenceClicked()
        assertEquals(EvidenceStatus.Loading, viewModel.uiState.value.evidenceStatus)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.Success)
        val success = viewModel.uiState.value.evidenceStatus as EvidenceStatus.Success
        assertEquals(1, success.evidence.allSources.size)
        assertEquals("pib.gov.in", success.evidence.allSources[0].domain)
        assertEquals(1, fakeEvidenceRepository.callCount)
    }

    @Test
    fun retrieveEvidence_handlesRepositoryFailure() = runTest(testDispatcher) {
        fakeEvidenceRepository.resultToReturn = Result.failure(RuntimeException("Network timeout"))

        viewModel.onClaimTextChanged("Some claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.Error)
        val error = viewModel.uiState.value.evidenceStatus as EvidenceStatus.Error
        assertEquals("Network timeout", error.message)
    }

    @Test
    fun retrieveEvidence_withEmptySources_returnsSuccessWithEmptySources() = runTest(testDispatcher) {
        fakeEvidenceRepository.resultToReturn = Result.success(
            com.example.samachaarsathi.data.model.EvidenceCollection(
                originalClaim = "Obscure claim",
                assertionEvidence = emptyList(),
                overallRetrievalStatus = com.example.samachaarsathi.data.model.RetrievalStatus.EMPTY_RESULTS,
            )
        )

        viewModel.onClaimTextChanged("Obscure claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.Success)
        val success = viewModel.uiState.value.evidenceStatus as EvidenceStatus.Success
        assertTrue(success.evidence.allSources.isEmpty())
    }

    @Test
    fun editingTextAfterEvidenceRetrieval_resetsEvidenceStatusToNotRetrieved() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Initial claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.Success)

        viewModel.onClaimTextChanged("Modified claim text")
        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.NotRetrieved)
    }

    @Test
    fun onVerifyClaimClicked_resetsPriorEvidenceStatusToNotRetrieved() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("First claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.Success)

        viewModel.onClaimTextChanged("Second claim")
        viewModel.onVerifyClaimClicked()
        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.NotRetrieved)
    }

    @Test
    fun retrieveEvidence_doesNothingWhenClaimIsBlank() = runTest(testDispatcher) {
        viewModel.onRetrieveEvidenceClicked()
        assertEquals(0, fakeEvidenceRepository.callCount)
        assertTrue(viewModel.uiState.value.evidenceStatus is EvidenceStatus.NotRetrieved)
    }

    @Test
    fun initialState_hasIdleVerdictStatus() {
        assertTrue(viewModel.uiState.value.verdictStatus is VerdictStatus.Idle)
    }

    @Test
    fun retrieveEvidence_chainsToVerdictSuccess() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Government launched scholarship portal")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.verdictStatus is VerdictStatus.Success)
        val verdict = (viewModel.uiState.value.verdictStatus as VerdictStatus.Success).verdict
        assertEquals(com.example.samachaarsathi.data.model.VerdictCategory.SUPPORTED, verdict.category)
    }

    @Test
    fun editingTextAfterVerdict_resetsVerdictStatusToIdle() = runTest(testDispatcher) {
        viewModel.onClaimTextChanged("Initial claim")
        viewModel.onVerifyClaimClicked()
        advanceUntilIdle()

        viewModel.onRetrieveEvidenceClicked()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.verdictStatus is VerdictStatus.Success)

        viewModel.onClaimTextChanged("Edited text")
        assertTrue(viewModel.uiState.value.verdictStatus is VerdictStatus.Idle)
    }

    @Test
    fun initialState_hasNoImageOrOcr() {
        assertNull(viewModel.uiState.value.selectedImageUri)
        assertNull(viewModel.uiState.value.ocrResult)
        assertFalse(viewModel.uiState.value.isOcrLoading)
    }

    @Test
    fun onImageSelected_populatesOcrResult() = runTest(testDispatcher) {
        val dummyUri = android.net.TestUri("content://fake/image_123.jpg")
        val dummyContext = ContextWrapper(null)

        viewModel.onImageSelected(dummyContext, dummyUri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(dummyUri, state.selectedImageUri)
        assertFalse(state.isOcrLoading)
        assertEquals("Fake OCR Extracted Text", state.ocrResult?.cleanedClaimText)
        assertTrue(state.ocrResult?.forwardingMetadata?.isForwarded == true)
    }

    @Test
    fun onImageRemoved_clearsImageAndOcrResult() = runTest(testDispatcher) {
        val dummyUri = android.net.TestUri("content://fake/image_123.jpg")
        viewModel.onImageSelected(ContextWrapper(null), dummyUri)
        advanceUntilIdle()

        viewModel.onImageRemoved()
        val state = viewModel.uiState.value
        assertNull(state.selectedImageUri)
        assertNull(state.ocrResult)
        assertFalse(state.isOcrLoading)
    }

    @Test
    fun onUseExtractedText_updatesClaimTextAndEnablesSubmission() {
        viewModel.onUseExtractedText("Extracted claim from screenshot")
        assertEquals("Extracted claim from screenshot", viewModel.uiState.value.claimText)
        assertTrue(viewModel.uiState.value.canSubmit)
    }
}
