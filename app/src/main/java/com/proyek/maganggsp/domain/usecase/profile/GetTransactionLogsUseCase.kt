// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/GetTransactionLogsUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import javax.inject.Inject

class GetTransactionLogsUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(ppid: String) = repository.getTransactionLogs(ppid)
}