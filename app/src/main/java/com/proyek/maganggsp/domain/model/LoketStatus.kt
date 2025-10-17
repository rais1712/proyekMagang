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
    NORMAL(
        displayName = "Normal",
        colorResource = "#2E7D32",           // green
        backgroundResource = "#E8F5E8"       // light green
    ),
    BLOCKED(
        displayName = "Diblokir",
        colorResource = "#D32F2F",           // red  
        backgroundResource = "#FFEBEE"       // light red
    ),
    FLAGGED(
        displayName = "Ditandai",
        colorResource = "#F57C00",           // orange
        backgroundResource = "#FFF3E0"       // light orange
    );

    companion object {
        /**
         * Convert from string (dari API response) ke enum
         */
        fun fromString(status: String?): LoketStatus {
            return when (status?.lowercase()) {
                "normal" -> NORMAL
                "blocked", "diblokir" -> BLOCKED
                "flagged", "ditandai" -> FLAGGED
                else -> NORMAL // default
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
            }
        }
    }
}
