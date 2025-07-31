package com.proyek.maganggsp.data.remote.dto

/**
 * Base response wrapper untuk semua response API
 */
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
