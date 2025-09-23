package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MODULAR: Block/Unblock operations use case
 */
class BlockUnblockUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    /**
     * Block profile by adding "blok" suffix to PPID
     */
    fun blockProfile(ppid: String): Flow<Resource<Unit>> {
        val blockedPpid = if (ppid.endsWith("blok")) ppid else "${ppid}blok"
        return profileRepository.updateProfile(ppid, blockedPpid)
    }

    /**
     * Unblock profile by removing "blok" suffix from PPID
     */
    fun unblockProfile(ppid: String): Flow<Resource<Unit>> {
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
            unblockProfile(ppid)
        } else {
            blockProfile(ppid)
        }
    }
}