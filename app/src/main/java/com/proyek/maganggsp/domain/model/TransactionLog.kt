package com.proyek.maganggsp.domain.model

data class TransactionLog(
    val tldRefnum: String,
    val tldPan: String,
    val tldIdpel: String,
    val tldAmount: Long,
    val tldBalance: Long,
    val tldDate: String,
    val tldPpid: String
)