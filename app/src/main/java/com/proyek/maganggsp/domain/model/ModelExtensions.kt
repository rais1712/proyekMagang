// File: app/src/main/java/com/proyek/maganggsp/domain/model/ModelExtensions.kt

package com.proyek.maganggsp.domain.model

/**
 * ✅ FIXED: Extension functions for domain models
 * ✅ FIXED: Property reference errors (amount vs tldAmount)
 */

// Receipt extensions
fun List<Receipt>.getTotalAmount(): Long = sumOf { it.mutasi }
fun List<Receipt>.getTotalMutasi(): Long = sumOf { it.mutasi }
fun List<Receipt>.getValidReceipts(): List<Receipt> = filter { it.hasValidData() }
fun List<Receipt>.sortByAmountDescending(): List<Receipt> = sortedByDescending { it.mutasi }
fun List<Receipt>.sortByRefNumber(): List<Receipt> = sortedBy { it.refNumber }
fun List<Receipt>.getRecentReceipts(limit: Int = 10): List<Receipt> = take(limit)

// TransactionLog extensions - FIXED: using correct property 'tldAmount'
fun List<TransactionLog>.calculateTotalAmount(): Long = sumOf { it.tldAmount }
fun List<TransactionLog>.getTotalIncoming(): Long =
    filter { it.isIncomingTransaction() }.sumOf { it.tldAmount }
fun List<TransactionLog>.getTotalOutgoing(): Long =
    filter { it.isOutgoingTransaction() }.sumOf { kotlin.math.abs(it.tldAmount) }
fun List<TransactionLog>.getNetAmount(): Long = sumOf { it.tldAmount }
fun List<TransactionLog>.getValidTransactions(): List<TransactionLog> = filter { it.hasValidData() }
fun List<TransactionLog>.sortByDateDescending(): List<TransactionLog> = sortedByDescending { it.tldTimestamp }
fun List<TransactionLog>.sortByAmountDescending(): List<TransactionLog> = sortedByDescending { it.tldAmount }
fun List<TransactionLog>.filterByDateRange(startDate: String, endDate: String): List<TransactionLog> =
    filter { it.tldTimestamp >= startDate && it.tldTimestamp <= endDate }
fun List<TransactionLog>.filterIncomingOnly(): List<TransactionLog> = filter { it.isIncomingTransaction() }
fun List<TransactionLog>.filterOutgoingOnly(): List<TransactionLog> = filter { it.isOutgoingTransaction() }

// Loket extensions
fun List<Loket>.getBlockedLokets(): List<Loket> = filter { it.isBlocked() }
fun List<Loket>.getNormalLokets(): List<Loket> = filter { it.isNormal() }
fun List<Loket>.getFlaggedLokets(): List<Loket> = filter { it.isFlagged() }
fun List<Loket>.getSuspendedLokets(): List<Loket> = filter { it.isSuspended() }
fun List<Loket>.sortByName(): List<Loket> = sortedBy { it.namaLoket }
fun List<Loket>.sortByPpid(): List<Loket> = sortedBy { it.ppid }
fun List<Loket>.sortBySaldo(): List<Loket> = sortedByDescending { it.saldoTerakhir }

// Common extensions
fun <T> List<T>.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()
fun <T> List<T>.getDebugSummary(): String = "List<${T::class.simpleName}>(size=$size)"
fun <T> List<T>.takeIfNotEmpty(count: Int): List<T> = if (isNotEmpty()) take(count) else emptyList()
fun <T> List<T>.getMiddleIndex(): Int = if (isEmpty()) 0 else size / 2
