// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/BaseRepository.kt
package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Base repository yang menyediakan common functionality untuk semua repository
 * FIXED: Removed @Inject from abstract class constructor
 */
abstract class BaseRepository(
    private val exceptionMapper: ExceptionMapper
) {

    /**
     * Generic function untuk API calls yang return Flow<Resource<T>>
     * Automatically handles loading state, error mapping, dan success state
     */
    protected fun <T> safeApiFlow(
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

    /**
     * Generic function untuk API calls yang return Response<T>
     * Handles HTTP response codes dan body extraction
     */
    protected fun <T> safeApiFlowWithResponse(
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

    /**
     * For actions that return Unit (like block/unblock/flag operations)
     */
    protected fun safeApiFlowUnit(
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

    /**
     * For API calls that return List<T> dan perlu di-map ke domain models
     */
    protected fun <T, R> safeApiFlowWithMapping(
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

    /**
     * For single item API calls that need mapping to domain models
     */
    protected fun <T, R> safeApiFlowWithItemMapping(
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