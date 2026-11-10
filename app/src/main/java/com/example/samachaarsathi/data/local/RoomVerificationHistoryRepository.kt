package com.example.samachaarsathi.data.local

import android.content.Context
import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Production implementation of [VerificationHistoryRepository] backed by Android Room Database.
 */
class RoomVerificationHistoryRepository(
    private val dao: VerificationHistoryDao,
) : VerificationHistoryRepository {

    override fun getAllHistory(): Flow<List<SessionVerificationItem>> {
        return dao.getAllHistory().map { entities ->
            entities.map { it.toSessionVerificationItem() }
        }
    }

    override fun getSavedHistory(): Flow<List<SessionVerificationItem>> {
        return dao.getSavedHistory().map { entities ->
            entities.map { it.toSessionVerificationItem() }
        }
    }

    override fun searchHistory(query: String): Flow<List<SessionVerificationItem>> {
        return dao.searchHistory(query).map { entities ->
            entities.map { it.toSessionVerificationItem() }
        }
    }

    override suspend fun saveVerification(item: SessionVerificationItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun saveVerifications(items: List<SessionVerificationItem>) {
        dao.upsertAll(items.map { it.toEntity() })
    }

    override suspend fun toggleSave(claimId: String) {
        val existing = dao.getById(claimId) ?: return
        dao.setSaved(claimId, !existing.isSaved)
    }

    override suspend fun setSaved(claimId: String, isSaved: Boolean) {
        dao.setSaved(claimId, isSaved)
    }

    override suspend fun deleteVerification(claimId: String) {
        dao.deleteById(claimId)
    }

    override suspend fun getVerificationById(claimId: String): SessionVerificationItem? {
        return dao.getById(claimId)?.toSessionVerificationItem()
    }

    override suspend fun getCount(): Int {
        return dao.getCount()
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }

    companion object {
        @Volatile
        private var INSTANCE: RoomVerificationHistoryRepository? = null

        fun getInstance(context: Context): RoomVerificationHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val database = SamachaarDatabase.getDatabase(context)
                val instance = RoomVerificationHistoryRepository(database.verificationHistoryDao())
                INSTANCE = instance
                instance
            }
        }
    }
}

internal fun VerificationHistoryEntity.toSessionVerificationItem(): SessionVerificationItem {
    val verdict = verdictJson?.let { VerdictJsonConverter.deserialize(it) }
    return SessionVerificationItem(
        id = id,
        claimText = claimText,
        timestampMs = timestampMs,
        verdict = verdict,
        isSaved = isSaved,
    )
}

internal fun SessionVerificationItem.toEntity(): VerificationHistoryEntity {
    return VerificationHistoryEntity(
        id = id,
        claimText = claimText,
        timestampMs = timestampMs,
        isSaved = isSaved,
        verdictCategory = verdict?.category?.name,
        confidenceLevel = verdict?.confidence?.level?.name,
        headline = verdict?.headline,
        verdictJson = verdict?.let { VerdictJsonConverter.serialize(it) },
    )
}
