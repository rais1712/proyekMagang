        tldPpid = tldPpid
    )
}
package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.TransactionLog

// DTO untuk response transaksi dari API
// Sesuaikan field dengan response API /trx/ppid/{ppid}
data class TransactionResponse(
    val tldRefnum: String,
    val tldPan: String,
    val tldIdpel: String,
    val tldAmount: Long,
    val tldBalance: Long,
    val tldDate: String,
    val tldPpid: String
)

fun TransactionResponse.toTransactionLog(): TransactionLog {
    return TransactionLog(
        tldRefnum = tldRefnum,
        tldPan = tldPan,
        tldIdpel = tldIdpel,
        tldAmount = tldAmount,
        tldBalance = tldBalance,
        tldDate = tldDate,

