package com.novasec.secureauth.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncryptionManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_ALIAS = "secureauth_master_key"
        private const val PREFS_NAME = "secureauth_encrypted_prefs"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store sensitive data encrypted
     */
    fun storeEncrypted(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Retrieve encrypted data
     */
    fun getEncrypted(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    /**
     * Remove encrypted data
     */
    fun removeEncrypted(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    /**
     * Clear all encrypted data (logout)
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Check if encryption is available and hardware-backed
     */
    fun isHardwareBacked(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null)
            if (entry == null) {
                // Generate a test key to check hardware support
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
                true
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Keys for storing auth data
    object AuthKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val EXPIRES_AT = "expires_at"
        const val USER_JSON = "user_json"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
