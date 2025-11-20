// File: app/src/main/java/com/proyek/maganggsp/domain/model/LoketStatus.kt

package com.proyek.maganggsp.domain.model

/**
 * Enum untuk status loket
 * Sesuai dengan UI mockup dan business logic
 */
enum class LoketStatus(
    val displayName: String,
    val colorResource: String,
    val backgroundResource: String
) {
    NORMAL("Normal", "#2E7D32", "#E8F5E8"),
    BLOCKED("Diblokir", "#D32F2F", "#FFEBEE"),
    FLAGGED("Ditandai", "#F57C00", "#FFF3E0"),
    SUSPENDED("Ditangguhkan", "#F57C00", "#FFF3E0"),
    UNKNOWN("Tidak Diketahui", "#757575", "#F5F5F5");

    companion object {
        /**
         * Convert from string (dari API response) ke enum
         */
        fun fromString(status: String?): LoketStatus {
            return when (status?.lowercase()) {
                "normal" -> NORMAL
                "blocked", "diblokir" -> BLOCKED
                "flagged", "ditandai" -> FLAGGED
                "suspended", "ditangguhkan" -> SUSPENDED
                else -> UNKNOWN // default
            }
        }

        /**
         * Convert untuk API request (block/unblock)
         */
        fun toApiString(status: LoketStatus): String {
            return when (status) {
                NORMAL -> "normal"
                BLOCKED -> "blocked"
                FLAGGED -> "flagged"
                SUSPENDED -> "suspended"
                UNKNOWN -> "unknown"
            }
        }
    }
}
