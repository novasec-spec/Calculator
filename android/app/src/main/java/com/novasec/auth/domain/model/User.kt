package com.novasec.auth.domain.model

import io.github.jan.supabase.gotrue.user.UserInfo

// Domain model for User
data class User(
    val id: String,
    val email: String?,
    val phone: String?,
    val createdAt: String?,
    val lastSignInAt: String?,
    val avatarUrl: String?,
    val displayName: String?
) {
    companion object {
        fun fromSupabaseUser(userInfo: UserInfo): User {
            return User(
                id = userInfo.id,
                email = userInfo.email,
                phone = userInfo.phone,
                createdAt = userInfo.createdAt,
                lastSignInAt = userInfo.lastSignInAt,
                avatarUrl = userInfo.userMetadata?.get("avatar_url")?.toString(),
                displayName = userInfo.userMetadata?.get("full_name")?.toString()
                    ?: userInfo.userMetadata?.get("name")?.toString()
            )
        }
    }
}

// Sealed class for auth results
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}

// Validation result
data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
