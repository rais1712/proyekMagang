package com.proyek.maganggsp.data.source.local

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.proyek.maganggsp.domain.model.Admin
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val TAG = "SessionManager"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val ADMIN_NAME_KEY = "admin_name"
        private const val ADMIN_EMAIL_KEY = "admin_email"
    }

    fun saveAuthToken(token: String) {
        try {
            sharedPreferences.edit().putString(AUTH_TOKEN_KEY, token).apply()
            Log.d(TAG, "Auth token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save auth token", e)
        }
    }

    fun getAuthToken(): String? {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
            Log.d(TAG, "Auth token retrieved: ${if (token != null) "EXISTS" else "NULL"}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve auth token", e)
            null
        }
    }

    fun saveAdminProfile(admin: Admin) {
        try {
            sharedPreferences.edit().apply {
                putString(ADMIN_NAME_KEY, admin.name)
                putString(ADMIN_EMAIL_KEY, admin.email)
                apply()
            }
            Log.d(TAG, "Admin profile saved successfully - Name: ${admin.name}, Email: ${admin.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save admin profile", e)
        }
    }

    fun getAdminProfile(): Admin? {
        return try {
            val name = sharedPreferences.getString(ADMIN_NAME_KEY, null)
            val email = sharedPreferences.getString(ADMIN_EMAIL_KEY, null)
            val token = getAuthToken()

            Log.d(TAG, "Retrieving admin profile - Name: ${name != null}, Email: ${email != null}, Token: ${token != null}")

            if (name != null && email != null && token != null) {
                val admin = Admin(name, email, token)
                Log.d(TAG, "Admin profile retrieved successfully")
                admin
            } else {
                Log.w(TAG, "Incomplete admin profile data - Name: $name, Email: $email, Token exists: ${token != null}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve admin profile", e)
            null
        }
    }

    fun clearSession() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "Session cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear session", e)
        }
    }

    // Debug helper function for testing
    fun debugSessionState(): String {
        val token = getAuthToken()
        val profile = getAdminProfile()
        return "Session State - Token: ${token != null}, Profile: ${profile != null}, " +
                "Name: ${profile?.name}, Email: ${profile?.email}"
    }
}