// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/GetRecentProfilesUseCase.kt
package com.proyek.maganggsp.domain.usecase.loketprofile

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UNIFIED: Get recent profiles from local history
 */
class GetRecentProfilesUseCase @Inject constructor(
    private val historyManager: LoketHistoryManager
) {
    operator fun invoke(): Flow<Resource<List<Receipt>>> = flow {
        try {
            emit(Resource.Loading())

            val recentHistory = historyManager.getRecentHistory()
            val receipts = recentHistory.map { history ->
                Receipt(
                    refNumber = "HISTORY-${history.ppid}",
                    idPelanggan = history.ppid,
                    amount = 0L, // History doesn't store amount
                    logged = history.getFormattedTanggalAkses(),
                    ppid = history.ppid,
                    namaLoket = history.namaLoket,
                    nomorHP = history.nomorHP,
                    email = history.email ?: "",
                    alamat = history.alamat ?: ""
                )
            }

            emit(Resource.Success(receipts))
        } catch (e: Exception) {
            emit(Resource.Error(
                com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                    "Gagal memuat riwayat: ${e.message}"
                )
            ))
        }
    }
}