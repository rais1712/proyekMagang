package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import javax.inject.Inject

class GetAdminProfileUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Admin? {
        return sessionManager.getAdminProfile()
    }
}