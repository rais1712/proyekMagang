// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/AuthRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.dto.LoginRequest  // ✅ CHANGED from data.api
import com.proyek.maganggsp.data.dto.LoginResponse  // ✅ CHANGED from data.api
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.exceptions.AppException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
    exceptionMapper: com.proyek.maganggsp.util.exceptions.ExceptionMapper
) : BaseRepository(exceptionMapper), AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun login(email: String, password: String): Admin {
        // ... rest of implementation stays same

        val request = LoginRequest(email, password)
        val response = api.login(request)

        val loginResponse = response.body()
            ?: throw AppException.ParseException("Empty response")

        // Convert to Admin
        val admin = Admin(
            token = loginResponse.token ?: "",
            email = loginResponse.email ?: "",
            name = loginResponse.nama ?: "Admin"
        )

        // Save session and return
        sessionManager.saveAuthToken(admin.token)
        sessionManager.saveAdminProfile(admin)

        return admin
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override fun isLoggedIn(): Boolean {
        return sessionManager.isSessionValid()
    }
}
