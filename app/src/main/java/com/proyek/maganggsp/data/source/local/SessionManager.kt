package com.proyek.maganggsp.data.source.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.proyek.maganggsp.domain.model.Admin
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    // ... (kode MasterKey dan sharedPreferences tetap sama)
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
        private const val AUTH_TOKEN_KEY = "auth_token"
        // <<< TAMBAHKAN KEY BARU UNTUK DATA ADMIN >>>
        private const val ADMIN_ID_KEY = "admin_id"
        private const val ADMIN_NAME_KEY = "admin_name"
        private const val ADMIN_EMAIL_KEY = "admin_email"
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    // <<< TAMBAHKAN FUNGSI BARU UNTUK MENYIMPAN PROFIL ADMIN >>>
    fun saveAdminProfile(admin: Admin) {
        sharedPreferences.edit().apply {
            putString(ADMIN_ID_KEY, admin.id)
            putString(ADMIN_NAME_KEY, admin.name)
            putString(ADMIN_EMAIL_KEY, admin.email)
            apply()
        }
    }

    // <<< TAMBAHKAN FUNGSI BARU UNTUK MENGAMBIL PROFIL ADMIN >>>
    fun getAdminProfile(): Admin? {
        val id = sharedPreferences.getString(ADMIN_ID_KEY, null)
        val name = sharedPreferences.getString(ADMIN_NAME_KEY, null)
        val email = sharedPreferences.getString(ADMIN_EMAIL_KEY, null)

        return if (id != null && name != null && email != null) {
            Admin(id, name, email)
        } else {
            null
        }
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}