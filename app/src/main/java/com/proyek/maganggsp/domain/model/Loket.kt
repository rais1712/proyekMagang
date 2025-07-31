package com.proyek.maganggsp.domain.model

/**
 * Model domain untuk data Loket (Mitra)
 */
data class Loket(
    val loketNumber: String,
    val phoneNumber: String,
    val loketName: String,
    val address: String,
    val status: LoketStatus,
    val lastAccessed: String,
    val hasFlaggedTransactions: Boolean
)

enum class LoketStatus {
    ACTIVE,
    BLOCKED
}
