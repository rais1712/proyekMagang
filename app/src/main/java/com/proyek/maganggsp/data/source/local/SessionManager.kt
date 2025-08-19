package com.proyek.maganggsp.data.source.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.proyek.maganggsp.domain.model.Admin
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var _sharedPreferences: SharedPreferences? = null
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
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val ADMIN_NAME_KEY = "admin_name"
        private const val ADMIN_EMAIL_KEY = "admin_email"
    }

    /**
     * Create SharedPreferences dengan fallback mechanism yang proper
     */
    private fun createSharedPreferences(): SharedPreferences {
        return try {
            createEncryptedPreferences()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create EncryptedSharedPreferences, using fallback", e)
            createFallbackPreferences()
        }
    }

    /**
     * Create EncryptedSharedPreferences
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
        ).also {
            Log.d(TAG, "EncryptedSharedPreferences created successfully")
        }
    }

    /**
     * Create normal SharedPreferences sebagai fallback
     */
    private fun createFallbackPreferences(): SharedPreferences {
        Log.w(TAG, "Using fallback SharedPreferences (not encrypted)")
        return context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(token: String?) {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Attempting to save null or blank token")
            return
        }

        try {
            val editor = sharedPreferences.edit()
            editor.putString(AUTH_TOKEN_KEY, token)
            val success = editor.commit() // Using commit() for immediate write
            if (success) {
                Log.d(TAG, "Auth token saved successfully")
            } else {
                Log.e(TAG, "Failed to save auth token - commit returned false")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while saving auth token", e)
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

    fun saveAdminProfile(admin: Admin?) {
        if (admin == null) {
            Log.w(TAG, "Attempting to save null admin profile")
            return
        }

        try {
            val editor = sharedPreferences.edit()
            editor.putString(ADMIN_NAME_KEY, admin.name)
            editor.putString(ADMIN_EMAIL_KEY, admin.email)
            val success = editor.commit()
            if (success) {
                Log.d(TAG, "Admin profile saved successfully - Name: ${admin.name}, Email: ${admin.email}")
            } else {
                Log.e(TAG, "Failed to save admin profile - commit returned false")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while saving admin profile", e)
        }
    }

    fun getAdminProfile(): Admin? {
        return try {
            val name = sharedPreferences.getString(ADMIN_NAME_KEY, null)
            val email = sharedPreferences.getString(ADMIN_EMAIL_KEY, null)
            val token = getAuthToken()

            Log.d(TAG, "Retrieving admin profile - Name: ${name != null}, Email: ${email != null}, Token: ${token != null}")

            if (!name.isNullOrBlank() && !email.isNullOrBlank() && !token.isNullOrBlank()) {
                val admin = Admin(name, email, token)
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

    fun clearSession() {
        try {
            val editor = sharedPreferences.edit()
            editor.clear()
            val success = editor.commit()
            if (success) {
                Log.d(TAG, "Session cleared successfully")
            } else {
                Log.e(TAG, "Failed to clear session - commit returned false")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while clearing session", e)
        }
    }

    /**
     * Check if session is valid (has token and profile)
     */
    fun isSessionValid(): Boolean {
        return try {
            val token = getAuthToken()
            val profile = getAdminProfile()
            val isValid = !token.isNullOrBlank() && profile != null
            Log.d(TAG, "Session validity check: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Exception while checking session validity", e)
            false
        }
    }

    /**
     * Check if session is expired
     * For now, just inverse of isSessionValid
     * TODO: Implement proper token expiry checking in future versions
     */
    fun isSessionExpired(): Boolean {
        return !isSessionValid()
    }

    /**
     * Debug helper function untuk testing dan troubleshooting
     */
    fun debugSessionState(): String {
        return try {
            val token = getAuthToken()
            val profile = getAdminProfile()
            val prefsType = if (sharedPreferences.javaClass.simpleName.contains("Encrypted")) {
                "Encrypted"
            } else {
                "Fallback"
            }
            "Session State ($prefsType) - Token: ${token != null}, Profile: ${profile != null}, " +
                    "Name: ${profile?.name}, Email: ${profile?.email}"
        } catch (e: Exception) {
            "Session State - Error: ${e.message}"
        }
    }
}