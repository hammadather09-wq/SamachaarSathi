package com.example.samachaarsathi.data.local

import com.example.samachaarsathi.data.model.DemoFixtures
import com.example.samachaarsathi.ui.claim.ClaimInputViewModel
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeVerificationHistoryRepository : VerificationHistoryRepository {
    private val storage = MutableStateFlow<Map<String, SessionVerificationItem>>(emptyMap())

    override fun getAllHistory(): Flow<List<SessionVerificationItem>> {
        return storage.asStateFlow().map { map ->
            map.values.sortedByDescending { it.timestampMs }
        }
    }

    override fun getSavedHistory(): Flow<List<SessionVerificationItem>> {
        return storage.asStateFlow().map { map ->
            map.values.filter { it.isSaved }.sortedByDescending { it.timestampMs }
        }
    }

    override fun searchHistory(query: String): Flow<List<SessionVerificationItem>> {
        return storage.asStateFlow().map { map ->
            map.values.filter {
                it.claimText.contains(query, ignoreCase = true) ||
                    (it.verdict?.headline?.contains(query, ignoreCase = true) == true)
            }.sortedByDescending { it.timestampMs }
        }
    }

    override suspend fun saveVerification(item: SessionVerificationItem) {
        val current = storage.value.toMutableMap()
        current[item.id] = item
        storage.value = current
    }

    override suspend fun saveVerifications(items: List<SessionVerificationItem>) {
        val current = storage.value.toMutableMap()
        for (item in items) {
            current[item.id] = item
        }
        storage.value = current
    }

    override suspend fun toggleSave(claimId: String) {
        val current = storage.value.toMutableMap()
        val existing = current[claimId] ?: return
        current[claimId] = existing.copy(isSaved = !existing.isSaved)
        storage.value = current
    }

    override suspend fun setSaved(claimId: String, isSaved: Boolean) {
        val current = storage.value.toMutableMap()
        val existing = current[claimId] ?: return
        current[claimId] = existing.copy(isSaved = isSaved)
        storage.value = current
    }

    override suspend fun deleteVerification(claimId: String) {
        val current = storage.value.toMutableMap()
        current.remove(claimId)
        storage.value = current
    }

    override suspend fun getVerificationById(claimId: String): SessionVerificationItem? {
        return storage.value[claimId]
    }

    override suspend fun getCount(): Int {
        return storage.value.size
    }

    override suspend fun clearAll() {
        storage.value = emptyMap()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationHistoryRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeVerificationHistoryRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeVerificationHistoryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repository_saveAndRetrieveHistory_returnsInChronologicalOrder() = runTest(testDispatcher) {
        val item1 = SessionVerificationItem(id = "1", claimText = "Older claim", timestampMs = 1000L)
        val item2 = SessionVerificationItem(id = "2", claimText = "Newer claim", timestampMs = 2000L)

        fakeRepo.saveVerifications(listOf(item1, item2))

        val all = fakeRepo.getAllHistory().first()
        assertEquals(2, all.size)
        assertEquals("2", all[0].id) // Newer first
        assertEquals("1", all[1].id)
    }

    @Test
    fun repository_bookmarkFilter_returnsOnlySaved() = runTest(testDispatcher) {
        val item1 = SessionVerificationItem(id = "1", claimText = "Claim 1", isSaved = false)
        val item2 = SessionVerificationItem(id = "2", claimText = "Claim 2", isSaved = true)

        fakeRepo.saveVerifications(listOf(item1, item2))

        val saved = fakeRepo.getSavedHistory().first()
        assertEquals(1, saved.size)
        assertEquals("2", saved[0].id)

        // Toggle item 1
        fakeRepo.toggleSave("1")
        val savedAfterToggle = fakeRepo.getSavedHistory().first()
        assertEquals(2, savedAfterToggle.size)
    }

    @Test
    fun repository_search_matchesClaimAndHeadline() = runTest(testDispatcher) {
        val item1 = SessionVerificationItem(
            id = "1",
            claimText = "Government laptop scheme announced",
            verdict = DemoFixtures.sampleVerdict,
        )
        val item2 = SessionVerificationItem(
            id = "2",
            claimText = "RBI 5000 note notification",
            verdict = DemoFixtures.sampleVerdictMisleading,
        )

        fakeRepo.saveVerifications(listOf(item1, item2))

        val searchResult = fakeRepo.searchHistory("laptop").first()
        assertEquals(1, searchResult.size)
        assertEquals("1", searchResult[0].id)
    }

    @Test
    fun viewModel_withHistoryRepository_persistsAndObservesHistory() = runTest(testDispatcher) {
        val item1 = SessionVerificationItem(
            id = "initial_1",
            claimText = "Initial persistent claim",
            verdict = DemoFixtures.sampleVerdict,
            isSaved = true,
        )
        fakeRepo.saveVerification(item1)

        val fakeGemini = object : com.example.samachaarsathi.data.GeminiRepository {
            override fun analyzeClaimStream(claim: String) = kotlinx.coroutines.flow.emptyFlow<String>()
            override suspend fun analyzeClaim(claim: String) = Result.success("")
        }
        val fakeUnderstanding = object : com.example.samachaarsathi.data.ClaimUnderstandingRepository {
            override suspend fun understandClaim(claim: String) = Result.success(
                com.example.samachaarsathi.data.model.ClaimUnderstanding(
                    originalClaim = claim,
                    coreClaim = claim,
                    claimType = "other",
                    keyAssertions = listOf(claim),
                )
            )
        }
        val fakeEvidence = object : com.example.samachaarsathi.data.EvidenceRetrievalRepository {
            override suspend fun retrieveEvidenceForUnderstanding(understanding: com.example.samachaarsathi.data.model.ClaimUnderstanding) =
                Result.success(com.example.samachaarsathi.data.model.EvidenceCollection(originalClaim = understanding.originalClaim))
            override suspend fun retrieveEvidenceForAssertion(assertionId: String, assertionText: String) =
                Result.success(com.example.samachaarsathi.data.model.AssertionEvidence(assertionId = assertionId, assertionText = assertionText))
        }

        val viewModel = ClaimInputViewModel(
            geminiRepository = fakeGemini,
            claimUnderstandingRepository = fakeUnderstanding,
            evidenceRetrievalRepository = fakeEvidence,
            historyRepository = fakeRepo,
        )
        advanceUntilIdle()

        // Verify initial state observed from repository
        assertEquals(1, viewModel.uiState.value.sessionHistory.size)
        assertEquals("initial_1", viewModel.uiState.value.sessionHistory[0].id)
        assertTrue(viewModel.uiState.value.sessionHistory[0].isSaved)

        // Toggle bookmark from ViewModel
        viewModel.onToggleSaveClaim("initial_1")
        advanceUntilIdle()

        // Verify updated in repository
        val inRepo = fakeRepo.getVerificationById("initial_1")
        assertNotNull(inRepo)
        assertFalse(inRepo!!.isSaved)
    }
}
