package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.HistoryApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.HistoryRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val api: HistoryApi
) : HistoryRepository {

    // FIXED: Menggunakan Flow<Resource<T>> pattern yang konsisten
    override fun getRecentHistory(): Flow<Resource<List<Loket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getRecentHistory()
            val loketList = response.map { it.toDomain() }
            emit(Resource.Success(loketList))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan yang tidak terduga."))
        }
    }

    // FIXED: Menggunakan Flow<Resource<T>> pattern yang konsisten
    override fun getFullHistory(): Flow<Resource<List<Loket>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getFullHistory()
            val loketList = response.map { it.toDomain() }
            emit(Resource.Success(loketList))
        } catch (e: HttpException) {
            emit(Resource.Error("Terjadi kesalahan: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Koneksi bermasalah, periksa jaringan internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan yang tidak terduga."))
        }
    }
}