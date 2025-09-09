// File: app/src/main/java/com/proyek/maganggsp/util/NavigationConstants.kt - FIXED FINAL
package com.proyek.maganggsp.util

/**
 * FIXED: Updated navigation constants untuk Receipt/TransactionLog data structure
 * Focus pada ppid-based navigation dengan Indonesian context
 */
object NavigationConstants {

    // ✅ NAVIGATION ARGUMENTS - Fixed untuk ppid flow
    const val ARG_PPID = "ppid"                       // PRIMARY: ppid identifier
    const val ARG_NO_LOKET = "noLoket"               // LEGACY: backward compatibility
    const val ARG_RECEIPT_REF = "receiptRef"         // Receipt reference
    const val ARG_CURRENT_PPID = "currentPpid"       // For update profile

    // ✅ SHARED PREFERENCES KEYS
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"
    const val PREF_ADMIN_ROLE = "admin_role"

    // ✅ INTENT EXTRAS
    const val EXTRA_PPID = "extra_ppid"
    const val EXTRA_RECEIPT_DATA = "extra_receipt_data"
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
        val TRANSACTION_LOG_ACTIVITY: Int
            get() = com.proyek.maganggsp.R.id.transactionLogActivity

        @JvmStatic
        val UPDATE_PROFILE_ACTIVITY: Int
            get() = com.proyek.maganggsp.R.id.updateProfileActivity
    }

    // ✅ ACTION IDs - Fixed untuk ppid navigation
    object Actions {
        @JvmStatic
        val HOME_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_homeFragment_to_transactionLogActivity

        @JvmStatic
        val HISTORY_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_historyFragment_to_transactionLogActivity

        @JvmStatic
        val MONITOR_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_monitorFragment_to_transactionLogActivity
    }

    // ✅ FRAGMENT TAGS
    const val TAG_HOME_FRAGMENT = "HomeFragment"
    const val TAG_HISTORY_FRAGMENT = "HistoryFragment"
    const val TAG_MONITOR_FRAGMENT = "MonitorFragment"
    const val TAG_TRANSACTION_LOG_ACTIVITY = "TransactionLogActivity"
    const val TAG_UPDATE_PROFILE_ACTIVITY = "UpdateProfileActivity"

    // ✅ BUNDLE KEYS
    const val BUNDLE_RECEIPT_LIST = "bundle_receipt_list"
    const val BUNDLE_TRANSACTION_LOGS = "bundle_transaction_logs"
    const val BUNDLE_SEARCH_QUERY = "bundle_search_query"
    const val BUNDLE_CURRENT_PPID = "bundle_current_ppid"

    // ✅ VALIDATION HELPERS
    fun isValidPpid(ppid: String?): Boolean {
        return !ppid.isNullOrBlank() && ppid.length >= 5
    }

    fun isValidReceiptRef(refNumber: String?): Boolean {
        return !refNumber.isNullOrBlank() && refNumber.length >= 3
    }

    // ✅ NAVIGATION HELPERS - Fixed untuk ppid flow
    fun createTransactionLogBundle(ppid: String): android.os.Bundle {
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

    fun createReceiptBundle(receiptRef: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_RECEIPT_REF, receiptRef)
        }
    }

    // ✅ ERROR MESSAGES - Indonesian
    object ErrorMessages {
        const val INVALID_PPID = "Format PPID tidak valid"
        const val NAVIGATION_FAILED = "Navigasi gagal"
        const val MISSING_ARGUMENT = "Argument navigasi tidak lengkap"
        const val INVALID_RECEIPT_REF = "Nomor referensi tidak valid"
    }

    // ✅ SUCCESS MESSAGES - Indonesian
    object SuccessMessages {
        const val NAVIGATION_SUCCESS = "Navigasi berhasil"
        const val PROFILE_UPDATED = "Profil berhasil diupdate"
        const val DATA_LOADED = "Data berhasil dimuat"
    }

    // ✅ DEBUG HELPERS
    fun getNavigationDebugInfo(): String {
        return """
        NavigationConstants Debug Info:
        - Primary Argument: $ARG_PPID
        - Available Destinations: ${Destinations::class.java.declaredFields.size}
        - Available Actions: ${Actions::class.java.declaredFields.size}
        - Core Flow: Home → TransactionLog → UpdateProfile
        - Language: Indonesian
        """.trimIndent()
    }

    fun validateNavigationArguments(bundle: android.os.Bundle?): String {
        if (bundle == null) return "Bundle is null"

        val ppid = bundle.getString(ARG_PPID)
        val noLoket = bundle.getString(ARG_NO_LOKET)
        val receiptRef = bundle.getString(ARG_RECEIPT_REF)

        return """
        Navigation Arguments Validation:
        - ppid: ${if (isValidPpid(ppid)) "✅ $ppid" else "❌ Invalid/Missing"}
        - noLoket (legacy): ${if (noLoket != null) "⚠️ $noLoket" else "❌ Missing"}
        - receiptRef: ${if (isValidReceiptRef(receiptRef)) "✅ $receiptRef" else "❌ Invalid/Missing"}
        
        Recommendation: ${when {
            isValidPpid(ppid) -> "Use ppid: $ppid"
            noLoket != null -> "Convert noLoket to ppid: $noLoket"
            else -> "Provide valid ppid argument"
        }}
        """.trimIndent()
    }
}

/**
 * FIXED: Extension functions untuk easier navigation dengan ppid
 */

// Navigation argument extensions - Fixed
fun android.os.Bundle.getPpid(): String? = getString(NavigationConstants.ARG_PPID)
fun android.os.Bundle.getReceiptRef(): String? = getString(NavigationConstants.ARG_RECEIPT_REF)
fun android.os.Bundle.getNoLoket(): String? = getString(NavigationConstants.ARG_NO_LOKET) // Legacy
fun android.os.Bundle.getCurrentPpid(): String? = getString(NavigationConstants.ARG_CURRENT_PPID)

// Bundle creation extensions - Fixed
fun String.toPpidNavigationBundle(): android.os.Bundle {
    return NavigationConstants.createTransactionLogBundle(this)
}

fun String.toUpdateProfileBundle(): android.os.Bundle {
    return NavigationConstants.createUpdateProfileBundle(this)
}

// Validation extensions - Fixed
fun String?.isValidNavigationPpid(): Boolean = NavigationConstants.isValidPpid(this)
fun String?.isValidReceiptReference(): Boolean = NavigationConstants.isValidReceiptRef(this)

// Safe navigation extension
fun String?.extractPpidSafely(fallbackPpid: String = "PIDLKTD0025blok"): String {
    return when {
        NavigationConstants.isValidPpid(this) -> this!!
        !this.isNullOrBlank() && this.length > 10 -> this
        else -> fallbackPpid
    }.also { result ->
        android.util.Log.d("NavigationConstants", "📋 Extracted ppid: $result from input: $this")
    }
}