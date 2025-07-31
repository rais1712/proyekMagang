package com.proyek.maganggsp.data.remote.dto

import java.math.BigDecimal
import java.util.Date

/**
 * DTO untuk data Loket dan Mutasi
 */
data class LoketDto(
    val loketNumber: String,
    val phoneNumber: String,
    val loketName: String,
    val address: String,
    val status: String,
    val lastAccessed: String,
    val hasFlaggedTransactions: Boolean
)

data class MutasiDto(
    val refNumber: String,
    val amount: BigDecimal,
    val timestamp: Date,
    val isFlagged: Boolean
)
