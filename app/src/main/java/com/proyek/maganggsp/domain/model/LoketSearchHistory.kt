// File: app/src/main/java/com/proyek/maganggsp/domain/model/LoketSearchHistory.kt - IMPORT FIXED
package com.proyek.maganggsp.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UPDATED: Model untuk loket search/access history - Search by PPID
 */
data class LoketSearchHistory(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val email: String? = null,
    val alamat: String? = null,
    val status: LoketStatus = LoketStatus.NORMAL,
    val tanggalAkses: Long = System.currentTimeMillis(),
    val jumlahAkses: Int = 1
) {

    fun getFormattedTanggalAkses(): String {
        val date = Date(tanggalAkses)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
        return format.format(date)
    }

    fun getDisplayText(): String = "$namaLoket - Diakses ${jumlahAkses}x"

    /**
     * UPDATED: Match PPID untuk search functionality
     */
    fun matchesPpidSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return ppid.lowercase().contains(lowerQuery)
    }

    /**
     * Convert to Loket object
     */
    fun toLoket(): Loket {
        return Loket(
            ppid = ppid,
            namaLoket = namaLoket,
            nomorHP = nomorHP,
            alamat = alamat ?: "",
            email = email ?: "",
            status = status,
            tanggalAkses = getFormattedTanggalAkses()
        )
    }
}