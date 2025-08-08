package com.proyek.maganggsp.domain.usecase.history

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.HistoryRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetRecentHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(): Flow<Resource<List<Loket>>> = flow {
        try {
            emit(Resource.Loading())
            val history = repository.getRecentHistory()
            emit(Resource.Success(history))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan yang tidak terduga"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        }
    }
}