package com.novasec.secureauth.repository

import android.content.Context
import com.novasec.secureauth.data.models.User
import com.novasec.secureauth.supabase.supabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.eq
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(private val context: Context) {

    private val supabase = context.supabaseClient()

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String, fullName: String): Result<User> {
        return try {
            // signUpWith(Email) returns UserInfo? directly in v3 — not a session wrapper
            val newUser = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // v3 expects a JsonObject, not a Map, for extra metadata
                data = buildJsonObject {
                    put("full_name", fullName)
                    put("created_at", System.currentTimeMillis().toString())
                }
            }

            // Save user to custom users table
            val user = createUserProfile(
                id = newUser?.id ?: "",
                email = email,
                fullName = fullName
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            // signInWith(Email) doesn't return the user — fetch it via currentUserOrNull() after
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""

            // Get user profile from database
            val user = getUserProfile(currentUserId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    /**
     * Create user profile in custom users table
     */
    private suspend fun createUserProfile(id: String, email: String, fullName: String): User {
        val user = User(
            id = id,
            email = email,
            fullName = fullName,
            createdAt = System.currentTimeMillis().toString()
        )

        // Store in profiles table
        supabase.postgrest["profiles"].insert(user)

        return user
    }

    /**
     * Get user profile from custom table
     */
    private suspend fun getUserProfile(userId: String): User {
        val response = supabase.postgrest["profiles"]
            .select {
                filter { eq("id", userId) }
            }
            .decodeList<User>()

        return response.firstOrNull() ?: throw Exception("User profile not found")
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            supabase.postgrest["profiles"]
                .update(user) {
                    filter { eq("id", user.id) }
                }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user by email (for offline caching)
     */
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val response = supabase.postgrest["profiles"]
                .select {
                    filter { eq("email", email) }
                }
                .decodeList<User>()

            response.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
