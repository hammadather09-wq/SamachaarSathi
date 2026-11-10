package com.example.samachaarsathi.data.local

import com.example.samachaarsathi.ui.claim.SessionVerificationItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing persistent verification history and bookmarked claims.
 */
interface VerificationHistoryRepository {

    /**
     * Flow of all verification history records ordered chronologically (newest first).
     */
    fun getAllHistory(): Flow<List<SessionVerificationItem>>

    /**
     * Flow of bookmarked / saved verification records ordered chronologically (newest first).
     */
    fun getSavedHistory(): Flow<List<SessionVerificationItem>>

    /**
     * Searches history by claim text or headline.
     */
    fun searchHistory(query: String): Flow<List<SessionVerificationItem>>

    /**
     * Inserts or updates a verification item.
     */
    suspend fun saveVerification(item: SessionVerificationItem)

    /**
     * Inserts or updates multiple verification items in batch.
     */
    suspend fun saveVerifications(items: List<SessionVerificationItem>)

    /**
     * Toggles bookmark state for the specified verification ID.
     */
    suspend fun toggleSave(claimId: String)

    /**
     * Sets explicit bookmark state for the specified verification ID.
     */
    suspend fun setSaved(claimId: String, isSaved: Boolean)

    /**
     * Deletes a verification item by ID.
     */
    suspend fun deleteVerification(claimId: String)

    /**
     * Retrieves a single verification item by ID, or null if not found.
     */
    suspend fun getVerificationById(claimId: String): SessionVerificationItem?

    /**
     * Returns total number of persisted verification records.
     */
    suspend fun getCount(): Int

    /**
     * Clears all history records (used for testing or explicit user reset).
     */
    suspend fun clearAll()
}
