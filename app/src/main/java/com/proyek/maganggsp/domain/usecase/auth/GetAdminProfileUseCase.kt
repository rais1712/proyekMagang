// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/GetAdminProfileUseCase.kt

package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import android.util.Log
import javax.inject.Inject

/**
 * Use case to get current admin profile from session
 * Returns null if no admin is logged in
 */
class GetAdminProfileUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "GetAdminProfileUseCase"
    }

    operator fun invoke(): Admin? {
        return try {
            val admin = sessionManager.getAdminProfile()
            if (admin != null) {
                Log.d(TAG, "Admin profile retrieved: ${admin.email}")
            } else {
                Log.d(TAG, "No admin profile found in session")
            }
            admin
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admin profile", e)
            null
        }
    }
}
