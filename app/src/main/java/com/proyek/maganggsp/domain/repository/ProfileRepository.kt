// File: app/src/main/java/com/proyek/maganggsp/domain/repository/ProfileRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * MODULAR: Profile Repository Interface
 * Handles profile-related operations for Receipt display
 */
interface ProfileRepository {

    /**
     * Get profile data (maps to Receipt for home screen cards)
     * Source: GET /profiles/ppid/{ppid}
     */
    fun getProfile(ppid: String): Flow<Resource<Receipt>>

    /**
     * Update profile for block/unblock operations
     * Source: PUT /profiles/ppid/{ppid} with {"mpPpid": "newValue"}
     */
    fun updateProfile(currentPpid: String, newPpid: String): Flow<Resource<Unit>>

    /**
     * Search profiles by PPID pattern (local history + direct API access)
     */
    fun searchProfiles(ppidQuery: String): Flow<Resource<List<Receipt>>>

    /**
     * Get recent profiles from local history
     */
    fun getRecentProfiles(): Flow<Resource<List<Receipt>>>
}



