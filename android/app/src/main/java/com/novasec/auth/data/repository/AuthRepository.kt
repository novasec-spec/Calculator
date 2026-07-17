package com.novasec.auth.data.repository

import com.novasec.auth.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val auth = SupabaseClient.auth

    // Sign In
    suspend fun signIn(email: String, password: String): Result<UserSession> =
        withContext(Dispatchers.IO) {
            try {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val session = auth.currentSessionOrNull()
                if (session != null) {
                    Result.success(session)
                } else {
                    Result.failure(Exception("No session returned after login"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Sign Up
    suspend fun signUp(email: String, password: String): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Sign Out
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reset Password
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Current Session
    fun getCurrentSession(): UserSession? = auth.currentSessionOrNull()

    fun getCurrentUser(): UserInfo? = auth.currentUserOrNull()

    fun isUserLoggedIn(): Boolean = auth.currentSessionOrNull() != null

    // Refresh Session
    suspend fun refreshSession(): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            auth.refreshCurrentSession()
            val session = auth.currentSessionOrNull()
            if (session != null) {
                Result.success(session)
            } else {
                Result.failure(Exception("Failed to refresh session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
