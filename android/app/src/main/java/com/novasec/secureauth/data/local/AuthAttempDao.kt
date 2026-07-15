package com.novasec.secureauth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthAttemptDao {
    @Query("SELECT COUNT(*) FROM auth_attempt WHERE email = :email AND timestamp > :since")
    suspend fun getAttemptCount(email: String, since: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AuthAttempt)

    @Query("DELETE FROM auth_attempt WHERE email = :email")
    suspend fun clearAttempts(email: String)

    @Query("DELETE FROM auth_attempt WHERE timestamp < :olderThan")
    suspend fun deleteOldAttempts(olderThan: Long)
}
