package com.proyek.maganggsp.util

import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog

object PlaceholderDataGenerator {
    
    fun createPlaceholderReceipt(ppid: String): Receipt {
        return Receipt(
            refNumber = "REF-PLACEHOLDER",
            noPelanggan = "PLG${ppid.take(6)}",
            tipe = "DEPOSIT",
            jumlah = 100000L,
            status = "SUCCESS",
            waktu = "2024-01-15T10:30:00.000Z",
            ppid = ppid,
            namaLoket = "Loket Placeholder",
            nomorHP = "081234567890",
            email = "placeholder@gespay.com",
            alamat = "Alamat Placeholder",
            loketStatus = LoketStatus.NORMAL
        )
    }
    
    fun createPlaceholderTransactionLogs(ppid: String, count: Int = 5): List<TransactionLog> {
        return (1..count).map { index ->
            TransactionLog(
                id = "TXN${index.toString().padStart(3, '0')}",
                refNumber = "REF-PLACEHOLDER-$index",
                noPelanggan = "PLG${ppid.take(6)}",
                amount = if (index % 2 == 0) 50000L else -25000L,
                type = if (index % 2 == 0) "CREDIT" else "DEBIT",
                timestamp = "2024-01-${(15 - index).toString().padStart(2, '0')}T10:30:00.000Z",
                description = "Transaksi Placeholder $index",
                status = "SUCCESS",
                ppid = ppid,
                saldo = 1000000L - (index * 25000L)
            )
        }
    }
    
    fun createPlaceholderReceipts(count: Int = 5): List<Receipt> {
        return (1..count).map { index ->
            Receipt(
                refNumber = "REF-${index.toString().padStart(5, '0')}",
                noPelanggan = "PLG${index.toString().padStart(6, '0')}",
                tipe = if (index % 2 == 0) "DEPOSIT" else "WITHDRAWAL",
                jumlah = 50000L * index,
                status = "SUCCESS",
                waktu = "2024-01-${index.toString().padStart(2, '0')}T10:30:00.000Z",
                ppid = "PIDLKTD${index.toString().padStart(4, '0')}",
                namaLoket = "Loket $index",
                nomorHP = "081234567${index.toString().padStart(3, '0')}",
                email = "loket$index@gespay.com",
                alamat = "Alamat Loket $index",
                loketStatus = when (index % 3) {
                    0 -> LoketStatus.NORMAL
                    1 -> LoketStatus.FLAGGED
                    else -> LoketStatus.BLOCKED
                }
            )
        }
    }
}
