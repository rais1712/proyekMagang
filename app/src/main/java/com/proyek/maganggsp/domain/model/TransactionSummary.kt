package com.proyek.maganggsp.domain.model

data class TransactionSummary(
    val totalCount: Int,
    val incomingCount: Int,
    val outgoingCount: Int,
    val totalIncoming: Long,
    val totalOutgoing: Long,
    val netAmount: Long,
    val latestBalance: Long
)

