// File: app/src/main/java/com/proyek/maganggsp/domain/model/ModelExtensions.kt - NEW
package com.proyek.maganggsp.domain.model

/**
 * ✅ PHASE 1: Extension functions for domain models
 */

// Receipt extensions
fun List<Receipt>.getTotalAmount(): Long = sumOf { it.amount }

fun List<Receipt>.getValidReceipts(): List<Receipt> = filter { it.hasValidData() }

fun List<Receipt>.sortByAmountDescending(): List<Receipt> = sortedByDescending { it.amount }

fun List<Receipt>.sortByRefNumber(): List<Receipt> = sortedBy { it.refNumber }

// TransactionLog extensions
fun List<TransactionLog>.getTotalIncoming(): Long =
    filter { it.isIncomingTransaction() }.sumOf { it.amount }

fun List<TransactionLog>.getTotalOutgoing(): Long =
    filter { it.isOutgoingTransaction() }.sumOf { kotlin.math.abs(it.amount) }

fun List<TransactionLog>.getNetAmount(): Long = sumOf { it.amount }

fun List<TransactionLog>.getValidTransactions(): List<TransactionLog> = filter { it.hasValidData() }

fun List<TransactionLog>.sortByDateDescending(): List<TransactionLog> = sortedByDescending { it.timestamp }

fun List<TransactionLog>.sortByAmountDescending(): List<TransactionLog> = sortedByDescending { it.amount }

fun List<TransactionLog>.filterByDateRange(startDate: String, endDate: String): List<TransactionLog> =
    filter { it.timestamp >= startDate && it.timestamp <= endDate }

fun List<TransactionLog>.filterIncomingOnly(): List<TransactionLog> = filter { it.isIncomingTransaction() }

fun List<TransactionLog>.filterOutgoingOnly(): List<TransactionLog> = filter { it.isOutgoingTransaction() }

// Common extensions
fun <T> List<T>.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

fun <T> List<T>.getDebugSummary(): String = "List<${this::class.simpleName}>(size=${size})"