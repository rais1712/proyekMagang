// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/BlockUnblockUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UNIFIED: Block/Unblock operations using profile update
 */
class BlockUnblockUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    /**
     * Block loket by adding "blok" suffix to PPID
     */
    fun blockLoket(ppid: String): Flow<Resource<Unit>> {
        val blockedPpid = if (ppid.endsWith("blok")) ppid else "${ppid}blok"
        return profileRepository.updateProfile(ppid, blockedPpid)
    }

    /**
     * Unblock loket by removing "blok" suffix from PPID
     */
    fun unblockLoket(ppid: String): Flow<Resource<Unit>> {
        val originalPpid = ppid.removeSuffix("blok")
        return profileRepository.updateProfile(ppid, originalPpid)
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
            unblockLoket(ppid)
        } else {
            blockLoket(ppid)
        }
    }
}