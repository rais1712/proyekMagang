package com.proyek.maganggsp.domain.model

/**
 * Enum untuk status loket dalam sistem
 * Digunakan untuk menentukan tampilan UI dan aksi yang tersedia
 */
enum class LoketStatus {
    NORMAL,
    BLOCKED,
    FLAGGED;
    
    companion object {
        /**
         * Menentukan status loket berdasarkan PPID
         * @param ppid PPID loket
         * @return Status loket berdasarkan PPID
         */
        fun fromPpid(ppid: String): LoketStatus {
            // Implementasi logika untuk menentukan status berdasarkan PPID
            return when {
                ppid.contains("block", ignoreCase = true) -> BLOCKED
                ppid.contains("flag", ignoreCase = true) -> FLAGGED
                else -> NORMAL
            }
        }
    }
}