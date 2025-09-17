// File: app/src/main/java/com/proyek/maganggsp/util/NavigationConstants.kt - PPID FOCUSED CLEAN
package com.proyek.maganggsp.util

/**
 * SIMPLIFIED: Navigation constants dengan focus pada PPID sebagai primary key
 * Eliminates confusion antara multiple argument types
 */
object NavigationConstants {

    // ✅ PRIMARY NAVIGATION ARGUMENT - PPID Only
    const val ARG_PPID = "ppid"

    // ✅ LEGACY SUPPORT - For backward compatibility only
    const val ARG_NO_LOKET = "noLoket" // Will map to PPID
    const val ARG_RECEIPT_REF = "receiptRef" // Will map to PPID
    const val ARG_CURRENT_PPID = "currentPpid" // For update operations

    // ✅ SHARED PREFERENCES KEYS
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"
    const val PREF_ADMIN_ROLE = "admin_role"

    // ✅ INTENT EXTRAS
    const val EXTRA_PPID = "extra_ppid"
    const val EXTRA_SESSION_EXPIRED = "extra_session_expired"
    const val EXTRA_UPDATE_RESULT = "extra_update_result"

    // ✅ REQUEST CODES
    const val REQUEST_CODE_LOGIN = 1001
    const val REQUEST_CODE_UPDATE_PROFILE = 1002

    // ✅ NAVIGATION DESTINATION IDs
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
            get() = com.proyek.maganggsp.R.id.transactionLogActivity
    }

    // ✅ ACTION IDs - PPID navigation focused
    object Actions {
        @JvmStatic
        val HOME_TO_DETAIL_LOKET: Int
            get() = com.proyek.maganggsp.R.id.action_homeFragment_to_transactionLogActivity

        @JvmStatic
        val HISTORY_TO_DETAIL_LOKET: Int
            get() = com.proyek.maganggsp.R.id.action_historyFragment_to_transactionLogActivity

        @JvmStatic
        val MONITOR_TO_DETAIL_LOKET: Int
            get() = com.proyek.maganggsp.R.id.action_monitorFragment_to_transactionLogActivity
    }

    // ✅ VALIDATION HELPERS
    fun isValidPpid(ppid: String?): Boolean {
        return !ppid.isNullOrBlank() && ppid.length >= 5
    }

    // ✅ NAVIGATION HELPERS - PPID focused
    fun createDetailLoketBundle(ppid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_PPID, ppid)
            android.util.Log.d("NavigationConstants", "📦 Created bundle dengan ppid: $ppid")
        }
    }

    fun createUpdateProfileBundle(currentPpid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_CURRENT_PPID, currentPpid)
            android.util.Log.d("NavigationConstants", "📦 Created update profile bundle dengan ppid: $currentPpid")
        }
    }

    // ✅ ERROR MESSAGES - Indonesian
    object ErrorMessages {
        const val INVALID_PPID = "Format PPID tidak valid"
        const val NAVIGATION_FAILED = "Navigasi gagal"
        const val MISSING_ARGUMENT = "Argument navigasi tidak lengkap"
    }

    // ✅ SUCCESS MESSAGES - Indonesian
    object SuccessMessages {
        const val NAVIGATION_SUCCESS = "Navigasi berhasil"
        const val PROFILE_UPDATED = "Profil berhasil diupdate"
        const val DATA_LOADED = "Data berhasil dimuat"
    }
}

/**
 * SIMPLIFIED: Extension functions untuk easier navigation dengan PPID focus
 */

// Navigation argument extensions - PPID focused
fun android.os.Bundle.getPpid(): String? = getString(NavigationConstants.ARG_PPID)
fun android.os.Bundle.getCurrentPpid(): String? = getString(NavigationConstants.ARG_CURRENT_PPID)

// Bundle creation extensions - PPID focused
fun String.toDetailLoketBundle(): android.os.Bundle {
    return NavigationConstants.createDetailLoketBundle(this)
}

fun String.toUpdateProfileBundle(): android.os.Bundle {
    return NavigationConstants.createUpdateProfileBundle(this)
}

// Validation extensions - PPID focused
fun String?.isValidNavigationPpid(): Boolean = NavigationConstants.isValidPpid(this)

// Safe PPID extraction
fun String?.extractPpidSafely(fallbackPpid: String = "PIDLKTD0025"): String {
    return when {
        NavigationConstants.isValidPpid(this) -> this!!
        !this.isNullOrBlank() && this.length > 5 -> this
        else -> fallbackPpid
    }.also { result ->
        android.util.Log.d("NavigationConstants", "📋 Extracted ppid: $result from input: $this")
    }
}