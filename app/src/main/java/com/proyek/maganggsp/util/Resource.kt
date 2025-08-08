// File: app/src/main/java/com/proyek/maganggsp/util/Resource.kt
package com.proyek.maganggsp.util


sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    // Ubah urutan parameter di sini
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message) // <<< SOLUSI
    class Empty<T> : Resource<T>()
}