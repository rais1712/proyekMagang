package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoketRepositoryImpl @Inject constructor(
    private val api: LoketApi
) : LoketRepository {

    override fun getLoketDetail(idLoket: String): Flow<Resource<Loket>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getLoketDetail(idLoket)
            emit(Resource.Success(response.toDomain()))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun getMutation(idLoket: String): Flow<Resource<List<Mutasi>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getMutation(idLoket)
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun searchLoket(query: String): Flow<Resource<List<Loket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.searchLoket(query)
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun blockLoket(idLoket: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.blockLoket(idLoket)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Gagal memblokir loket: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun unblockLoket(idLoket: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.unblockLoket(idLoket)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Gagal membuka blokir loket: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun flagMutation(idMutasi: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.flagMutation(idMutasi)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Gagal menandai mutasi: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun clearAllFlags(idLoket: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.clearAllFlags(idLoket)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Gagal menghapus semua tanda: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun getFlaggedLokets(): Flow<Resource<List<Loket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getFlaggedLokets()
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }

    override fun getBlockedLokets(): Flow<Resource<List<Loket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getBlockedLokets()
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        }
    }
}