package com.novasec.secureauth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user")
data class CachedUser(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val lastLogin: Long,
    val roles: List<String> = emptyList()
)

@Entity(tableName = "auth_attempt")
data class AuthAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val timestamp: Long,
    val success: Boolean
)
