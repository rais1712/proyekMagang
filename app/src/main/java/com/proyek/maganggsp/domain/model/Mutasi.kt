package com.proyek.maganggsp.domain.model

import java.math.BigDecimal
import java.util.Date

/**
 * Model domain untuk data Mutasi (Transaksi)
 */
data class Mutasi(
    val refNumber: String,
    val amount: BigDecimal,
    val timestamp: Date,
    val isFlagged: Boolean
)
