package com.proyek.maganggsp.data.source.local

import java.text.SimpleDateFormat
import java.util.*

// Model untuk menyimpan riwayat akses loket/profile
// Sesuaikan field dengan kebutuhan repository

data class LoketHistory(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String?,
    val email: String?,
    val tanggalAkses: String // ISO timestamp
) {
    fun getFormattedTanggalAkses(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tanggalAkses)
            val readableFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tanggalAkses
        }
    }
}

