package com.novasec.secureauth.data.repository

import com.yourapp.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    
    private val auth = SupabaseClient.auth
    
    suspend fun signIn(email: String, password: String): Result<UserSession> = 
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val session = auth.currentSessionOrNull()
                if (session != null) {
                    Result.success(session)
                } else {
                    Result.failure(Exception("Login failed: No session returned"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
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
    
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isUserLoggedIn(): Boolean = auth.currentSessionOrNull() != null
    
    fun getCurrentUser(): UserInfo? = auth.currentUserOrNull()
}
