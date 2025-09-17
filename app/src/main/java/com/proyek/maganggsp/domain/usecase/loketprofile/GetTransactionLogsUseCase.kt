// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/GetTransactionLogsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loketprofile

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UNIFIED: Get transaction logs for detail screen
 */
class GetTransactionLogsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return profileRepository.getTransactionLogs(ppid)
    }
}