package com.novasec.secureauth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_user WHERE id = :userId")
    suspend fun getUser(userId: String): CachedUser?

    @Query("SELECT * FROM cached_user WHERE email = :email")
    suspend fun getUserByEmail(email: String): CachedUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: CachedUser)

    @Query("DELETE FROM cached_user")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM cached_user")
    fun getAllUsers(): Flow<List<CachedUser>>
}
