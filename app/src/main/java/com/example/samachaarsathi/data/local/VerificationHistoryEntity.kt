package com.example.samachaarsathi.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a persistent verification history or bookmarked claim record.
 *
 * Indexed by [timestampMs] for fast chronological ordering and [isSaved] for instant filtering of bookmarks.
 */
@Entity(
    tableName = "verification_history",
    indices = [
        Index(value = ["timestampMs"]),
        Index(value = ["isSaved"]),
    ],
)
data class VerificationHistoryEntity(
    @PrimaryKey
    val id: String,
    val claimText: String,
    val timestampMs: Long,
    val isSaved: Boolean = false,
    val verdictCategory: String? = null,
    val confidenceLevel: String? = null,
    val headline: String? = null,
    val verdictJson: String? = null,
)
