// File: app/src/main/java/com/proyek/maganggsp/util/exceptions/ExceptionMapper.kt
package com.proyek.maganggsp.util.exceptions

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ExceptionMapper {

    fun toAppException(exception: Exception): AppException {
        return when (exception) {
            is HttpException -> mapHttpException(exception)
            is IOException -> mapIOException(exception)
            is AppException -> exception
            else -> AppException.UnknownException(exception.message ?: "Unknown error")
        }
    }

    private fun mapHttpException(exception: HttpException): AppException {
        return when (exception.code()) {
            401 -> AppException.AuthenticationException("Unauthorized")
            403 -> AppException.AuthenticationException("Forbidden")
            404 -> AppException.ServerException(404, "Not found")
            500 -> AppException.ServerException(500, "Server error")
            else -> AppException.ServerException(exception.code(), "HTTP error")
        }
    }

    private fun mapIOException(exception: IOException): AppException {
        return when (exception) {
            is UnknownHostException -> AppException.NetworkException("Server unreachable")
            is ConnectException -> AppException.NetworkException("Connection failed")
            is SocketTimeoutException -> AppException.NetworkException("Connection timeout")
            else -> AppException.NetworkException("Network error: ${exception.message}")
        }
    }
}
