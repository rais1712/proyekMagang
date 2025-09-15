// File: app/src/main/java/com/proyek/maganggsp/domain/model/LoketSearchHistory.kt - NEW
package com.proyek.maganggsp.domain.model

import java.util.Date
import java.util.Locale

/**
 * NEW: Model untuk loket search/access history
 * Since we can't search by phone, track PPID access history
 */
data class LoketSearchHistory(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val tanggalAkses: Long = System.currentTimeMillis(),
    val jumlahAkses: Int = 1
) {

    fun getFormattedTanggalAkses(): String {
        val date = Date(tanggalAkses)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
        return format.format(date)
    }

    fun getDisplayText(): String = "$namaLoket - Diakses ${jumlahAkses}x"
}