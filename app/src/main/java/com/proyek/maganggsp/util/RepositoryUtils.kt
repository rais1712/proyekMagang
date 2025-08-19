// File: app/src/main/java/com/proyek/maganggsp/util/RepositoryUtils.kt
package com.proyek.maganggsp.util

import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Alternative utility functions untuk repository operations
 * Jika BaseRepository approach bermasalah, gunakan ini sebagai gantinya
 */
object RepositoryUtils {

    fun <T> safeApiFlow(
        exceptionMapper: ExceptionMapper,
        apiCall: suspend () -> T
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading())

        try {
            val result = apiCall()
            emit(Resource.Success(result))
        } catch (e: HttpException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: IOException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: Exception) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        }
    }

    fun <T> safeApiFlowWithResponse(
        exceptionMapper: ExceptionMapper,
        apiCall: suspend () -> Response<T>
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(AppException.ParseException("Response body is null")))
                }
            } else {
                val httpException = HttpException(response)
                val appException = exceptionMapper.mapToAppException(httpException)
                emit(Resource.Error(appException))
            }
        } catch (e: HttpException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: IOException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: Exception) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        }
    }

    fun safeApiFlowUnit(
        exceptionMapper: ExceptionMapper,
        apiCall: suspend () -> Response<Unit>
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiCall()
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val httpException = HttpException(response)
                val appException = exceptionMapper.mapToAppException(httpException)
                emit(Resource.Error(appException))
            }
        } catch (e: HttpException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: IOException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: Exception) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        }
    }

    fun <T, R> safeApiFlowWithMapping(
        exceptionMapper: ExceptionMapper,
        apiCall: suspend () -> List<T>,
        mapper: (T) -> R
    ): Flow<Resource<List<R>>> = flow {
        emit(Resource.Loading())

        try {
            val result = apiCall()
            val mappedResult = result.map(mapper)
            emit(Resource.Success(mappedResult))
        } catch (e: HttpException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: IOException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: Exception) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        }
    }

    fun <T, R> safeApiFlowWithItemMapping(
        exceptionMapper: ExceptionMapper,
        apiCall: suspend () -> T,
        mapper: (T) -> R
    ): Flow<Resource<R>> = flow {
        emit(Resource.Loading())

        try {
            val result = apiCall()
            val mappedResult = mapper(result)
            emit(Resource.Success(mappedResult))
        } catch (e: HttpException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: IOException) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        } catch (e: Exception) {
            val appException = exceptionMapper.mapToAppException(e)
            emit(Resource.Error(appException))
        }
    }
}