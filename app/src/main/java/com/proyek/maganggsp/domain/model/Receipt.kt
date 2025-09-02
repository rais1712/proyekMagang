package com.proyek.maganggsp.domain.model

/**
 * NEW DOMAIN MODEL: Receipt - replaces Loket-focused data structure
 * This represents receipt data from /profiles/ppid/{ppid}
 * Action: navigate to log detail when clicked
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val amount: Long,
    val logged: String
)

/**
 * NEW DOMAIN MODEL: TransactionLog - replaces Mutasi data structure
 * This represents detailed transaction logs from /trx/ppid/{ppid}
 * NOTE: message field is NOT displayed as per requirements
 */
