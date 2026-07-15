package com.novasec.secureauth.security

import android.content.Context
import com.google.gson.Gson
import com.novasec.secureauth.data.local.CacheDatabase
import com.novasec.secureauth.data.local.CachedUser
import com.novasec.secureauth.data.models.AuthSession
import com.novasec.secureauth.data.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionManager(private val context: Context) {
    private val encryptionManager = EncryptionManager(context)
    private val gson = Gson()
    private val db = CacheDatabase.getInstance(context)

    companion object {
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val MAX_ATTEMPTS = 5
    }

    /**
     * Save auth session encrypted
     */
    suspend fun saveSession(session: AuthSession) {
        withContext(Dispatchers.IO) {
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.ACCESS_TOKEN,
                session.accessToken
            )
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.REFRESH_TOKEN,
                session.refreshToken
            )
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.EXPIRES_AT,
                session.expiresAt.toString()
            )
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.USER_JSON,
                gson.toJson(session.user)
            )
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.USER_EMAIL,
                session.user.email
            )
            encryptionManager.storeEncrypted(
                EncryptionManager.AuthKeys.USER_NAME,
                session.user.fullName
            )

            // Cache user in Room for offline access
            db.userDao().insertUser(
                CachedUser(
                    id = session.user.id,
                    email = session.user.email,
                    fullName = session.user.fullName,
                    createdAt = session.user.createdAt,
                    lastLogin = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Get current session
     */
    suspend fun getSession(): AuthSession? = withContext(Dispatchers.IO) {
        val accessToken = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.ACCESS_TOKEN)
        val refreshToken = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.REFRESH_TOKEN)
        val expiresAtStr = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.EXPIRES_AT)
        val userJson = encryptionManager.getEncrypted(EncryptionManager.AuthKeys.USER_JSON)

        return@withContext if (accessToken != null && refreshToken != null && expiresAtStr != null && userJson != null) {
            val expiresAt = expiresAtStr.toLongOrNull()
            val user = gson.fromJson(userJson, User::class.java)
            if (expiresAt != null && user != null) {
                AuthSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt,
                    user = user
                )
            } else null
        } else null
    }

    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return encryptionManager.getEncrypted(EncryptionManager.AuthKeys.ACCESS_TOKEN)
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return encryptionManager.getEncrypted(EncryptionManager.AuthKeys.REFRESH_TOKEN)
    }

    /**
     * Check if session is valid and not expired
     */
    suspend fun isSessionValid(): Boolean = withContext(Dispatchers.IO) {
        val session = getSession()
        return@withContext session != null && session.expiresAt > System.currentTimeMillis()
    }

    /**
     * Check if session needs refresh (within 5 minutes of expiry)
     */
    suspend fun needsRefresh(): Boolean = withContext(Dispatchers.IO) {
        val session = getSession()
        return@withContext session != null && (session.expiresAt - System.currentTimeMillis()) < 5 * 60 * 1000
    }

    /**
     * Clear session (logout)
     */
    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            encryptionManager.clearAll()
            db.userDao().deleteAllUsers()
        }
    }

    /**
     * Rate limiting - check if user is locked out
     */
    suspend fun isLockedOut(email: String): Boolean = withContext(Dispatchers.IO) {
        val since = System.currentTimeMillis() - LOCKOUT_DURATION_MS
        val attempts = db.authAttemptDao().getAttemptCount(email, since)
        return@withContext attempts >= MAX_ATTEMPTS
    }

    /**
     * Record auth attempt for rate limiting
     */
    suspend fun recordAuthAttempt(email: String, success: Boolean) {
        withContext(Dispatchers.IO) {
            // Clear old attempts
            val olderThan = System.currentTimeMillis() - LOCKOUT_DURATION_MS
            db.authAttemptDao().deleteOldAttempts(olderThan)

            // Record new attempt
            db.authAttemptDao().insertAttempt(
                com.novasec.secureauth.data.local.AuthAttempt(
                    email = email,
                    timestamp = System.currentTimeMillis(),
                    success = success
                )
            )

            // If successful, clear attempts for this email
            if (success) {
                db.authAttemptDao().clearAttempts(email)
            }
        }
    }

    /**
     * Set biometric preference
     */
    fun setBiometricEnabled(enabled: Boolean) {
        encryptionManager.storeEncrypted(
            EncryptionManager.AuthKeys.BIOMETRIC_ENABLED,
            enabled.toString()
        )
    }

    /**
     * Check if biometric is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return encryptionManager.getEncrypted(EncryptionManager.AuthKeys.BIOMETRIC_ENABLED)?.toBoolean() ?: false
    }

    /**
     * Get cached user from Room (offline-first)
     */
    suspend fun getCachedUser(email: String): CachedUser? {
        return withContext(Dispatchers.IO) {
            db.userDao().getUserByEmail(email)
        }
    }
}
