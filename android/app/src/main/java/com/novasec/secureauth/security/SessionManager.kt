package com.novasec.secureauth.security

import android.content.Context
import com.google.gson.Gson
import com.novasec.secureauth.data.local.CacheDatabase
import com.novasec.secureauth.data.local.CachedUser
import com.novasec.secureauth.data.models.User
import com.novasec.secureauth.repository.AuthRepository
import com.novasec.secureauth.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(private val context: Context) {
    private val encryptionManager = EncryptionManager(context)
    private val gson = Gson()
    private val db = CacheDatabase.getInstance(context)
    private val authRepository = AuthRepository(context)
    private val supabase = context.supabaseClient()

    companion object {
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val MAX_ATTEMPTS = 5
    }

    /**
     * Save auth session encrypted
     */
    suspend fun saveSession(user: User) {
        withContext(Dispatchers.IO) {
            try {
                encryptionManager.storeEncrypted(
                    EncryptionManager.AuthKeys.USER_JSON,
                    gson.toJson(user)
                )
                encryptionManager.storeEncrypted(
                    EncryptionManager.AuthKeys.USER_EMAIL,
                    user.email
                )
                encryptionManager.storeEncrypted(
                    EncryptionManager.AuthKeys.USER_NAME,
                    user.fullName
                )
                encryptionManager.storeEncrypted(
                    EncryptionManager.AuthKeys.EXPIRES_AT,
                    (System.currentTimeMillis() + 3600 * 1000).toString() // 1 hour
                )

                // Cache user in Room for offline access
                db.userDao().insertUser(
                    CachedUser(
                        id = user.id,
                        email = user.email,
                        fullName = user.fullName,
                        createdAt = user.createdAt,
                        lastLogin = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Get current session
     */
    suspend fun getSession(): User? = withContext(Dispatchers.IO) {
        try {
            val userJson = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.USER_JSON)
            if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get current user (with Supabase session check)
     */
    suspend fun getCurrentUser(): User? {
        return try {
            if (supabase.auth.currentUserOrNull() != null) {
                getSession()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if session is valid
     */
    suspend fun isSessionValid(): Boolean = withContext(Dispatchers.IO) {
        try {
            val expiresAtStr = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.EXPIRES_AT)
            val expiresAt = expiresAtStr?.toLongOrNull() ?: 0
            
            // Check Supabase session too
            val isAuthenticated = supabase.auth.currentUserOrNull() != null
            
            expiresAt > System.currentTimeMillis() && isAuthenticated
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear session (logout)
     */
    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            try {
                encryptionManager.clearAll()
                db.userDao().deleteAllUsers()
                authRepository.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Rate limiting - check if user is locked out
     */
    suspend fun isLockedOut(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val since = System.currentTimeMillis() - LOCKOUT_DURATION_MS
            val attempts = db.authAttemptDao().getAttemptCount(email, since)
            attempts >= MAX_ATTEMPTS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Record auth attempt for rate limiting
     */
    suspend fun recordAuthAttempt(email: String, success: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                val olderThan = System.currentTimeMillis() - LOCKOUT_DURATION_MS
                db.authAttemptDao().deleteOldAttempts(olderThan)

                db.authAttemptDao().insertAttempt(
                    com.novasec.secureauth.data.local.AuthAttempt(
                        email = email,
                        timestamp = System.currentTimeMillis(),
                        success = success
                    )
                )

                if (success) {
                    db.authAttemptDao().clearAttempts(email)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Set biometric preference
     */
    fun setBiometricEnabled(enabled: Boolean) {
        try {
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.BIOMETRIC_ENABLED,
                enabled.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return try {
            encryptionManager.getEncrypted(EncryptionManager.AuthKeys.BIOMETRIC_ENABLED)?.toBoolean() ?: false
        } catch (e: Exception) {
            false
        }
À    }

    /**
     * Get cached user from Room (offline-first)
     */
    suspend fun getCachedUser(email: String): CachedUser? {
        return withContext(Dispatchers.IO) {
            try {
                db.userDao().getUserByEmail(email)
            } catch (e: Exception) {
                null
            }
        }
    }
}
