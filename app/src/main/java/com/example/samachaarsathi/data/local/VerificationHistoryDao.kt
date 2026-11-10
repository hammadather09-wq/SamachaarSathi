package com.example.samachaarsathi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for persistent verification history and bookmarks.
 */
@Dao
interface VerificationHistoryDao {

    @Query("SELECT * FROM verification_history ORDER BY timestampMs DESC")
    fun getAllHistory(): Flow<List<VerificationHistoryEntity>>

    @Query("SELECT * FROM verification_history WHERE isSaved = 1 ORDER BY timestampMs DESC")
    fun getSavedHistory(): Flow<List<VerificationHistoryEntity>>

    @Query("SELECT * FROM verification_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VerificationHistoryEntity?

    @Query("SELECT * FROM verification_history WHERE claimText LIKE '%' || :query || '%' OR headline LIKE '%' || :query || '%' ORDER BY timestampMs DESC")
    fun searchHistory(query: String): Flow<List<VerificationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VerificationHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<VerificationHistoryEntity>)

    @Query("UPDATE verification_history SET isSaved = :isSaved WHERE id = :id")
    suspend fun setSaved(id: String, isSaved: Boolean)

    @Query("DELETE FROM verification_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM verification_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM verification_history")
    suspend fun getCount(): Int
}
