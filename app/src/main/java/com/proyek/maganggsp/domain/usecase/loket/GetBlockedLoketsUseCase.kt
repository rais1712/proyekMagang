package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetBlockedLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(): Flow<Resource<List<Loket>>> = flow {
        try {
            emit(Resource.Loading())
            val lokets = repository.getBlockedLokets()
            emit(Resource.Success(lokets))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan yang tidak terduga"))
        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server."))
        }
    }
}