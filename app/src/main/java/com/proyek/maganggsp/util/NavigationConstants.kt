// File: app/src/main/java/com/proyek/maganggsp/util/NavigationConstants.kt
package com.proyek.maganggsp.util

/**
 * SIMPLIFIED: Constants untuk navigation arguments dan keys yang benar-benar digunakan
 * Menghapus konstanta yang redundant dan tidak terpakai
 */
object NavigationConstants {

    // ✅ NAVIGATION ARGUMENTS (Dipakai di nav_graph.xml)
    const val ARG_NO_LOKET = "noLoket"

    // ✅ SHARED PREFERENCES KEYS (Dipakai di SessionManager)
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"

    // ✅ NAVIGATION DESTINATION IDs (Runtime values, bukan compile-time constants)
    // Menggunakan @JvmStatic untuk akses yang mudah tapi bukan const
    object Destinations {
        @JvmStatic
        val HOME_FRAGMENT: Int
            get() = com.proyek.maganggsp.R.id.homeFragment

        @JvmStatic
        val HISTORY_FRAGMENT: Int
            get() = com.proyek.maganggsp.R.id.historyFragment

        @JvmStatic
        val MONITOR_FRAGMENT: Int
            get() = com.proyek.maganggsp.R.id.monitorFragment

        @JvmStatic
        val DETAIL_LOKET_ACTIVITY: Int
            get() = com.proyek.maganggsp.R.id.detailLoketActivity
    }

    // ✅ INTENT EXTRAS (Untuk fallback navigation jika diperlukan)
    const val EXTRA_NO_LOKET = "extra_no_loket"
    const val EXTRA_SESSION_EXPIRED = "extra_session_expired"
}

/**
 * REMOVED (Tidak dipakai):
 * - REQUEST_CODE_* → Pakai Navigation Component, bukan startActivityForResult
 * - TAG_*_FRAGMENT → Fragment tags tidak diperlukan dengan Navigation Component
 * - ACTION_* → Action IDs sudah ada di nav_graph.xml, tidak perlu konstanta terpisah
 * - ARG_LOKET_DATA → Cuma perlu noLoket aja, data diambil via API
 * - EXTRA_FROM_SEARCH → Tidak digunakan di implementasi sekarang
 */