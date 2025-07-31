package com.proyek.maganggsp.util

sealed class NetworkResult<T> {
    data class Success<T>(val data: T): NetworkResult<T>()
    data class Error<T>(
        val code: Int? = null,
        val message: String? = null,
        val networkMessage: String? = null
    ): NetworkResult<T>()
    class Loading<T>: NetworkResult<T>()
    class Idle<T>: NetworkResult<T>()
}
