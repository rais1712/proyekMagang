package com.proyek.maganggsp.data.source.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.util.NavigationConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var _sharedPreferences: SharedPreferences? = null
    private var _isEncrypted: Boolean = false

    private val sharedPreferences: SharedPreferences
        get() {
            if (_sharedPreferences == null) {
                _sharedPreferences = createSharedPreferences()
            }
            return _sharedPreferences!!
        }

    companion object {
        private const val TAG = "SessionManager"
        private const val ENCRYPTED_PREFS_NAME = "encrypted_session"
        private const val FALLBACK_PREFS_NAME = "session_fallback"

        // FIXED: Menggunakan constants dari NavigationConstants
        private const val AUTH_TOKEN_KEY = NavigationConstants.PREF_AUTH_TOKEN
        private const val ADMIN_NAME_KEY = NavigationConstants.PREF_ADMIN_NAME
        private const val ADMIN_EMAIL_KEY = NavigationConstants.PREF_ADMIN_EMAIL

        // Session validity duration (24 hours in milliseconds)
        private const val SESSION_DURATION_MS = 24 * 60 * 60 * 1000L
        private const val SESSION_TIMESTAMP_KEY = "session_timestamp"
    }

    /**
     * ENHANCED: Create SharedPreferences dengan better error handling dan logging
     */
    private fun createSharedPreferences(): SharedPreferences {
        return try {
            val encryptedPrefs = createEncryptedPreferences()
            _isEncrypted = true
            Log.i(TAG, "Successfully created EncryptedSharedPreferences")
            encryptedPrefs
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create EncryptedSharedPreferences, using fallback", e)
            _isEncrypted = false
            createFallbackPreferences()
        }
    }

    /**
     * Create EncryptedSharedPreferences dengan better error handling
     */
    @Throws(Exception::class)
    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Create normal SharedPreferences sebagai fallback
     */
    private fun createFallbackPreferences(): SharedPreferences {
        Log.w(TAG, "Using fallback SharedPreferences (not encrypted)")
        return context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * ENHANCED: Save auth token dengan session timestamp
     */
    fun saveAuthToken(token: String?): Boolean {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Attempting to save null or blank token")
            return false
        }

        return try {
            val currentTime = System.currentTimeMillis()
            val editor = sharedPreferences.edit()
            editor.putString(AUTH_TOKEN_KEY, token)
            editor.putLong(SESSION_TIMESTAMP_KEY, currentTime)

            val success = editor.commit()
            if (success) {
                Log.d(TAG, "Auth token saved successfully with timestamp: $currentTime")
            } else {
                Log.e(TAG, "Failed to save auth token - commit returned false")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception while saving auth token", e)
            false
        }
    }

    fun getAuthToken(): String? {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
            Log.d(TAG, "Auth token retrieved: ${if (token != null) "EXISTS" else "NULL"}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Exception while retrieving auth token", e)
            null
        }
    }

    /**
     * ENHANCED: Save admin profile dengan better validation
     */
    fun saveAdminProfile(admin: Admin?): Boolean {
        if (admin == null) {
            Log.w(TAG, "Attempting to save null admin profile")
            return false
        }

        if (admin.name.isBlank() || admin.email.isBlank()) {
            Log.w(TAG, "Attempting to save admin profile with blank name or email")
            return false
        }

        return try {
            val editor = sharedPreferences.edit()
            editor.putString(ADMIN_NAME_KEY, admin.name.trim())
            editor.putString(ADMIN_EMAIL_KEY, admin.email.trim())

            val success = editor.commit()
            if (success) {
                Log.d(TAG, "Admin profile saved successfully - Name: ${admin.name}, Email: ${admin.email}")
            } else {
                Log.e(TAG, "Failed to save admin profile - commit returned false")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception while saving admin profile", e)
            false
        }
    }

    fun getAdminProfile(): Admin? {
        return try {
            val name = sharedPreferences.getString(ADMIN_NAME_KEY, null)
            val email = sharedPreferences.getString(ADMIN_EMAIL_KEY, null)
            val token = getAuthToken()

            Log.d(TAG, "Retrieving admin profile - Name: ${name != null}, Email: ${email != null}, Token: ${token != null}")

            if (!name.isNullOrBlank() && !email.isNullOrBlank() && !token.isNullOrBlank()) {
                val admin = Admin(name.trim(), email.trim(), token)
                Log.d(TAG, "Admin profile retrieved successfully")
                admin
            } else {
                Log.w(TAG, "Incomplete admin profile data - Name: $name, Email: $email, Token exists: ${token != null}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while retrieving admin profile", e)
            null
        }
    }

    /**
     * ENHANCED: Clear session dengan better cleanup
     */
    fun clearSession(): Boolean {
        return try {
            val editor = sharedPreferences.edit()
            editor.clear()
            val success = editor.commit()
            if (success) {
                Log.d(TAG, "Session cleared successfully")
            } else {
                Log.e(TAG, "Failed to clear session - commit returned false")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception while clearing session", e)
            false
        }
    }

    /**
     * ENHANCED: Check session validity dengan time-based expiration
     */
    fun isSessionValid(): Boolean {
        return try {
            val token = getAuthToken()
            val profile = getAdminProfile()
            val sessionTimestamp = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
            val currentTime = System.currentTimeMillis()

            val hasValidData = !token.isNullOrBlank() && profile != null
            val isNotExpired = sessionTimestamp > 0L && (currentTime - sessionTimestamp) < SESSION_DURATION_MS

            val isValid = hasValidData && isNotExpired

            Log.d(TAG, "Session validity check: $isValid (hasData: $hasValidData, notExpired: $isNotExpired)")

            if (hasValidData && !isNotExpired) {
                Log.i(TAG, "Session expired, clearing session data")
                clearSession()
            }

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Exception while checking session validity", e)
            false
        }
    }

    /**
     * ENHANCED: More comprehensive session expiry check
     */
    fun isSessionExpired(): Boolean {
        return !isSessionValid()
    }

    /**
     * NEW: Get remaining session time in minutes
     */
    fun getRemainingSessionTimeMinutes(): Long {
        return try {
            val sessionTimestamp = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
            if (sessionTimestamp == 0L) return 0L

            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - sessionTimestamp
            val remainingTime = SESSION_DURATION_MS - elapsedTime

            if (remainingTime <= 0) 0L else remainingTime / (1000 * 60) // Convert to minutes
        } catch (e: Exception) {
            Log.e(TAG, "Exception while calculating remaining session time", e)
            0L
        }
    }

    /**
     * ENHANCED: Debug helper dengan lebih banyak informasi
     */
    fun debugSessionState(): String {
        return try {
            val token = getAuthToken()
            val profile = getAdminProfile()
            val sessionTimestamp = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
            val remainingTime = getRemainingSessionTimeMinutes()
            val prefsType = if (_isEncrypted) "Encrypted" else "Fallback"

            "Session State ($prefsType) - " +
                    "Token: ${token != null}, " +
                    "Profile: ${profile != null}, " +
                    "Name: ${profile?.name}, " +
                    "Email: ${profile?.email}, " +
                    "Remaining: ${remainingTime}min"
        } catch (e: Exception) {
            "Session State - Error: ${e.message}"
        }
    }
}