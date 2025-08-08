// File: app/src/main/java/com/proyek/maganggsp/data/dto/Mapper.kt
package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi

// Mengubah AdminDto menjadi Admin (Model Domain)
fun AdminDto.toDomain(): Admin {
    return Admin(
        id = this.id,
        name = this.fullName,
        email = this.email
    )
}

// Mengubah LoketDto menjadi Loket (Model Domain)
fun LoketDto.toDomain(): Loket {
    return Loket(
        id = this.id,
        name = this.loketName,
        phoneNumber = this.phoneNumber,
        address = this.address,
        balance = this.lastBalance,
        status = this.currentStatus,
        lastAccessed = this.lastAccessedDate
    )
}

// Mengubah MutasiDto menjadi Mutasi (Model Domain)
fun MutasiDto.toDomain(): Mutasi {
    return Mutasi(
        id = this.transactionId,
        description = this.notes,
        amount = this.amount,
        type = this.transactionType,
        balanceAfter = this.balanceAfter,
        reference = this.referenceCode,
        timestamp = this.transactionDate,
        isFlagged = this.isFlagged
    )
}