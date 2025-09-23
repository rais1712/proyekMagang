// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/UnifiedUseCases.kt
package com.proyek.maganggsp.domain.usecase

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.UnifiedRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * COMPLETE USE CASE CONSOLIDATION
 * Eliminates scattered use case classes, creates unified business logic layer
 * Removes: GetProfileUseCase, GetTransactionLogsUseCase, BlockUnblockUseCase, dll
 * Creates: Single comprehensive use case dengan semua operations
 */

// ============================================================================
// PROFILE/RECEIPT USE CASES
// ============================================================================

/**
 * Get profile data (maps to Receipt for home screen)
 */
class GetProfileUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<Receipt>> {
        return repository.getProfile(ppid)
    }
}

/**
 * Search profiles by PPID pattern
 */
class SearchProfilesUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {
    operator fun invoke(ppidQuery: String): Flow<Resource<List<Receipt>>> {
        return repository.searchProfiles(ppidQuery)
    }

    /**
     * PPID validation helpers
     */
    data class ValidationResult(
        val isValid: Boolean,
        val isError: Boolean,
        val message: String
    )

    fun validatePpid(ppid: String): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, true, "PPID tidak boleh kosong")
            ppid.length < 5 -> ValidationResult(false, false, "Ketik minimal 5 karakter PPID")
            ppid.length < 8 -> ValidationResult(false, true, "PPID terlalu pendek")
            !isValidPpidFormat(ppid) -> {
                ValidationResult(false, true, "Format PPID tidak valid. Gunakan format PIDLKTD0025")
            }
            else -> ValidationResult(true, false, "Valid")
        }
    }

    private fun isValidPpidFormat(ppid: String): Boolean {
        val patterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(),
            "^[A-Z]{3,}[0-9]+.*$".toRegex()
        )
        return patterns.any { it.matches(ppid) }
    }

    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "PIDLKTD0030"
        )
    }
}

/**
 * Get recent profiles from local history
 */
class GetRecentProfilesUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {
    operator fun invoke(): Flow<Resource<List<Receipt>>> {
        return repository.getRecentProfiles()
    }
}

// ============================================================================
// TRANSACTION LOG USE CASES
// ============================================================================

/**
 * Get transaction logs for detail screen
 */
class GetTransactionLogsUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return repository.getTransactionLogs(ppid)
    }
}

// ============================================================================
// BLOCK/UNBLOCK USE CASES
// ============================================================================

/**
 * Block/Unblock operations using profile update
 */
class BlockUnblockUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {

    /**
     * Block profile by adding "blok" suffix to PPID
     */
    fun blockProfile(ppid: String): Flow<Resource<Unit>> {
        return repository.blockProfile(ppid)
    }

    /**
     * Unblock profile by removing "blok" suffix from PPID
     */
    fun unblockProfile(ppid: String): Flow<Resource<Unit>> {
        return repository.unblockProfile(ppid)
    }

    /**
     * Check if PPID is blocked
     */
    fun isBlocked(ppid: String): Boolean {
        return ppid.endsWith("blok")
    }

    /**
     * Toggle block status
     */
    fun toggleBlockStatus(ppid: String): Flow<Resource<Unit>> {
        return if (isBlocked(ppid)) {
            unblockProfile(ppid)
        } else {
            blockProfile(ppid)
        }
    }
}

/**
 * Update profile dengan custom PPID
 */
class UpdateProfileUseCase @Inject constructor(
    private val repository: UnifiedRepository
) {
    operator fun invoke(currentPpid: String, newPpid: String): Flow<Resource<Unit>> {
        return repository.updateProfile(currentPpid, newPpid)
    }
}

// ============================================================================
// AUTH USE CASES (Keep existing, but streamlined)
// ============================================================================

/**
 * Login use case - KEEP EXISTING
 */
class LoginUseCase @Inject constructor(
    private val authRepository: com.proyek.maganggsp.domain.repository.AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<com.proyek.maganggsp.domain.model.Admin>> {
        return flow {
            try {
                emit(Resource.Loading())

                // Enhanced input validation
                when {
                    email.isBlank() -> {
                        emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.ValidationException("Email tidak boleh kosong")))
                        return@flow
                    }
                    password.isBlank() -> {
                        emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.ValidationException("Password tidak boleh kosong")))
                        return@flow
                    }
                    !isValidEmail(email) -> {
                        emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.ValidationException("Format email tidak valid")))
                        return@flow
                    }
                    password.length < 6 -> {
                        emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.ValidationException("Password minimal 6 karakter")))
                        return@flow
                    }
                }

                val admin = authRepository.login(email.trim(), password)
                emit(Resource.Success(admin))

            } catch (e: com.proyek.maganggsp.util.exceptions.AppException) {
                emit(Resource.Error(e))
            } catch (e: Exception) {
                emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.UnknownException("Terjadi kesalahan yang tidak terduga")))
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * Logout use case - KEEP EXISTING
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: com.proyek.maganggsp.domain.repository.AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return flow {
            try {
                emit(Resource.Loading())
                authRepository.logout()
                emit(Resource.Success(Unit))
            } catch (e: com.proyek.maganggsp.util.exceptions.AppException) {
                emit(Resource.Error(e))
            } catch (e: Exception) {
                emit(Resource.Error(com.proyek.maganggsp.util.exceptions.AppException.UnknownException("Gagal melakukan logout: ${e.message}")))
            }
        }
    }
}

/**
 * Get admin profile use case - KEEP EXISTING
 */
class GetAdminProfileUseCase @Inject constructor(
    private val sessionManager: com.proyek.maganggsp.data.source.local.SessionManager
) {
    operator fun invoke(): com.proyek.maganggsp.domain.model.Admin? {
        return sessionManager.getAdminProfile()
    }
}

/**
 * Login status check use case - KEEP EXISTING
 */
class IsLoggedInUseCase @Inject constructor(
    private val authRepository: com.proyek.maganggsp.domain.repository.AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}