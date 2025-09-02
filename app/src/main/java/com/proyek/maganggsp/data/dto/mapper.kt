// File: app/src/main/java/com/proyek/maganggsp/data/dto/mapper.kt - UPDATED
package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog

// EXISTING: LoginResponse -> Admin mapping (keep unchanged)
fun LoginResponse.toDomain(): Admin {
    return Admin(
        name = extractNameFromEmail(this.email),
        email = this.email ?: "Email tidak tersedia",
        token = this.token ?: ""
    )
}

private fun extractNameFromEmail(email: String?): String {
    return if (!email.isNullOrBlank() && email.contains("@")) {
        val localPart = email.substringBefore("@")
        localPart.replace("[._]".toRegex(), " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    } else {
        "Admin User"
    }
}

// NEW: ProfileResponse -> Receipt mapping
fun ProfileResponse.toDomain(): Receipt {
    return Receipt(
        refNumber = this.refNumber ?: "-",
        idPelanggan = this.idPelanggan ?: "-",
        amount = this.amount ?: 0L,
        logged = this.logged ?: "-"
    )
}

// NEW: TransactionResponse -> TransactionLog mapping
fun TransactionResponse.toDomain(): TransactionLog {
    return TransactionLog(
        tldRefnum = this.tldRefnum ?: "-",
        tldPan = this.tldPan ?: "-",
        tldIdpel = this.tldIdpel ?: "-",
        tldAmount = this.tldAmount ?: 0L,
        tldBalance = this.tldBalance ?: 0L,
        tldDate = this.tldDate ?: "-",
        tldPpid = this.tldPpid ?: "-"
    )
}

// DEPRECATED: Legacy mappers (keep for backward compatibility during transition)
// LoketDto.toDomain() - will be removed after complete migration
// MutasiDto.toDomain() - will be removed after complete migration