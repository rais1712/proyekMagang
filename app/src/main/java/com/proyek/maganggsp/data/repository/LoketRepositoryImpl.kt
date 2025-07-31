package com.proyek.maganggsp.data.repository

import com.proyek.maganggsp.data.remote.api.LoketApi
import com.proyek.maganggsp.data.remote.api.HistoryApi
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkException
import com.proyek.maganggsp.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoketRepositoryImpl @Inject constructor(
    private val api: LoketApi,
    private val historyApi: HistoryApi
) : LoketRepository {

    override suspend fun getLoketByPhone(phoneNumber: String): NetworkResult<Loket> {
        return try {
            val response = api.getLoketByPhone(phoneNumber)
            if (response.isSuccessful) {
                val loketDto = response.body()
                if (loketDto != null) {
                    NetworkResult.Success(
                        Loket(
                            loketNumber = loketDto.loketNumber,
                            phoneNumber = loketDto.phoneNumber,
                            loketName = loketDto.loketName,
                            address = loketDto.address,
                            status = if (loketDto.status == "BLOCKED") LoketStatus.BLOCKED else LoketStatus.ACTIVE,
                            lastAccessed = loketDto.lastAccessed,
                            hasFlaggedTransactions = loketDto.hasFlaggedTransactions
                        )
                    )
                } else {
                    NetworkResult.Error(
                        code = response.code(),
                        message = "Loket tidak ditemukan",
                        networkMessage = response.message()
                    )
                }
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = when(response.code()) {
                        404 -> "Loket tidak ditemukan"
                        401 -> "Sesi telah berakhir, silakan login kembali"
                        in 500..599 -> "Terjadi kesalahan pada server"
                        else -> "Terjadi kesalahan"
                    },
                    networkMessage = response.message()
                )
            }
        } catch (e: Exception) {
            val networkException = e.toNetworkException()
            NetworkResult.Error(
                message = when(networkException) {
                    is NetworkException.Connection -> "Tidak ada koneksi internet"
                    is NetworkException.Timeout -> "Koneksi timeout"
                    is NetworkException.UnknownHost -> "Tidak dapat terhubung ke server"
                    else -> "Terjadi kesalahan"
                }
            )
        }
    }

    override suspend fun getMutations(loketNumber: String): NetworkResult<List<Mutasi>> {
        return try {
            val response = api.getLoketMutations(loketNumber)
            if (response.isSuccessful) {
                val mutasiList = response.body()?.map { dto ->
                    Mutasi(
                        refNumber = dto.refNumber,
                        amount = dto.amount,
                        timestamp = dto.timestamp,
                        isFlagged = dto.isFlagged
                    )
                } ?: emptyList()
                NetworkResult.Success(mutasiList)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = when(response.code()) {
                        404 -> "Data mutasi tidak ditemukan"
                        401 -> "Sesi telah berakhir"
                        else -> "Gagal mengambil data mutasi"
                    }
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat mengambil data mutasi")
        }
    }

    override suspend fun blockLoket(loketNumber: String): NetworkResult<Unit> {
        return try {
            val response = api.blockLoket(loketNumber)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal memblokir loket"
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat memblokir loket")
        }
    }

    override suspend fun unblockLoket(loketNumber: String): NetworkResult<Unit> {
        return try {
            val response = api.unblockLoket(loketNumber)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal membuka blokir loket"
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat membuka blokir loket")
        }
    }

    override suspend fun getFlaggedLokets(): NetworkResult<List<Loket>> {
        return try {
            val response = api.getFlaggedLokets()
            if (response.isSuccessful) {
                val loketList = response.body()?.map { dto ->
                    Loket(
                        loketNumber = dto.loketNumber,
                        phoneNumber = dto.phoneNumber,
                        loketName = dto.loketName,
                        address = dto.address,
                        status = if (dto.status == "BLOCKED") LoketStatus.BLOCKED else LoketStatus.ACTIVE,
                        lastAccessed = dto.lastAccessed,
                        hasFlaggedTransactions = dto.hasFlaggedTransactions
                    )
                } ?: emptyList()
                NetworkResult.Success(loketList)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal mengambil daftar loket yang ditandai"
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat mengambil daftar loket")
        }
    }

    override suspend fun getBlockedLokets(): NetworkResult<List<Loket>> {
        return try {
            val response = api.getBlockedLokets()
            if (response.isSuccessful) {
                val loketList = response.body()?.map { dto ->
                    Loket(
                        loketNumber = dto.loketNumber,
                        phoneNumber = dto.phoneNumber,
                        loketName = dto.loketName,
                        address = dto.address,
                        status = LoketStatus.BLOCKED,
                        lastAccessed = dto.lastAccessed,
                        hasFlaggedTransactions = dto.hasFlaggedTransactions
                    )
                } ?: emptyList()
                NetworkResult.Success(loketList)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal mengambil daftar loket yang diblokir"
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat mengambil daftar loket")
        }
    }

    override fun getRecentHistory(): Flow<NetworkResult<List<Loket>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = historyApi.getRecentHistory()
            if (response.isSuccessful) {
                val loketList = response.body()?.map { dto ->
                    Loket(
                        loketNumber = dto.loketNumber,
                        phoneNumber = dto.phoneNumber,
                        loketName = dto.loketName,
                        address = dto.address,
                        status = if (dto.status == "BLOCKED") LoketStatus.BLOCKED else LoketStatus.ACTIVE,
                        lastAccessed = dto.lastAccessed,
                        hasFlaggedTransactions = dto.hasFlaggedTransactions
                    )
                } ?: emptyList()
                emit(NetworkResult.Success(loketList))
            } else {
                emit(NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal mengambil riwayat terakhir"
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(message = "Terjadi kesalahan saat mengambil riwayat"))
        }
    }

    override fun getFullHistory(): Flow<NetworkResult<List<Loket>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = historyApi.getFullHistory()
            if (response.isSuccessful) {
                val loketList = response.body()?.map { dto ->
                    Loket(
                        loketNumber = dto.loketNumber,
                        phoneNumber = dto.phoneNumber,
                        loketName = dto.loketName,
                        address = dto.address,
                        status = if (dto.status == "BLOCKED") LoketStatus.BLOCKED else LoketStatus.ACTIVE,
                        lastAccessed = dto.lastAccessed,
                        hasFlaggedTransactions = dto.hasFlaggedTransactions
                    )
                } ?: emptyList()
                emit(NetworkResult.Success(loketList))
            } else {
                emit(NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal mengambil riwayat lengkap"
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(message = "Terjadi kesalahan saat mengambil riwayat"))
        }
    }

    override suspend fun flagTransaction(mutationId: String): NetworkResult<Unit> {
        return try {
            val response = api.flagTransaction(mutationId)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    code = response.code(),
                    message = "Gagal menandai transaksi sebagai mencurigakan"
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Terjadi kesalahan saat menandai transaksi")
        }
    }
}
