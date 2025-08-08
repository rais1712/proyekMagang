package com.proyek.maganggsp.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {

    /**
     * Mengubah angka Long menjadi format mata uang Rupiah (Rp 1.500.000).
     */
    fun toRupiah(value: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0 // Menghilangkan desimal
        return numberFormat.format(value)
    }

    /**
     * Mengubah string tanggal ISO menjadi format "14 Mei 2024, 14:30".
     */
    fun toReadableDateTime(isoString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(isoString)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date)
        } catch (e: Exception) {
            isoString // Kembalikan string asli jika format tidak sesuai
        }
    }
}