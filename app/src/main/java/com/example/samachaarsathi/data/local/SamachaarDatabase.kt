package com.example.samachaarsathi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room Database for SamachaarSathi.
 *
 * Persists verification history and bookmarked claims locally on-device using SQLite.
 */
@Database(
    entities = [VerificationHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SamachaarDatabase : RoomDatabase() {

    abstract fun verificationHistoryDao(): VerificationHistoryDao

    companion object {
        const val DATABASE_NAME = "samachaarsathi_database.db"

        @Volatile
        private var INSTANCE: SamachaarDatabase? = null

        fun getDatabase(context: Context): SamachaarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SamachaarDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
