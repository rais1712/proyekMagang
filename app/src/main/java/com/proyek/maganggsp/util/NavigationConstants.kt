// File: app/src/main/java/com/proyek/maganggsp/util/NavigationConstants.kt - PHASE 2 UPDATE
package com.proyek.maganggsp.util

/**
 * ✅ PHASE 2: Updated navigation constants for Receipt/TransactionLog data structure
 * Simplified and streamlined - removed unused constants
 */
object NavigationConstants {

    // ✅ NAVIGATION ARGUMENTS - Updated for new data structure
    const val ARG_PPID = "ppid"                    // NEW: For receipt/transaction identification
    const val ARG_NO_LOKET = "noLoket"            // LEGACY: Keep for backward compatibility
    const val ARG_RECEIPT_REF = "receiptRef"       // NEW: For receipt reference numbers

    // ✅ SHARED PREFERENCES KEYS - Keep existing
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"
    const val PREF_ADMIN_ROLE = "admin_role"       // NEW: Store user role

    // ✅ INTENT EXTRAS - Updated for new structure
    const val EXTRA_PPID = "extra_ppid"
    const val EXTRA_RECEIPT_DATA = "extra_receipt_data"
    const val EXTRA_SESSION_EXPIRED = "extra_session_expired"
    const val EXTRA_TRANSACTION_REF = "extra_transaction_ref"

    // ✅ REQUEST CODES - Simplified
    const val REQUEST_CODE_LOGIN = 1001
    const val REQUEST_CODE_TRANSACTION_DETAIL = 1002

    // ✅ NAVIGATION DESTINATION IDs - Runtime values with better naming
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

        // LEGACY: Keep for backward compatibility
        @JvmStatic
        val DETAIL_LOKET_ACTIVITY: Int
            get() = com.proyek.maganggsp.R.id.transactionLogActivity
    }

    // ✅ ACTION IDs - Updated for new navigation structure
    object Actions {
        @JvmStatic
        val HOME_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_homeFragment_to_detailLoketActivity

        @JvmStatic
        val HISTORY_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_historyFragment_to_transactionLogActivity

        @JvmStatic
        val MONITOR_TO_TRANSACTION_LOG: Int
            get() = com.proyek.maganggsp.R.id.action_monitorFragment_to_transactionLogActivity
    }

    // ✅ FRAGMENT TAGS - Simplified
    const val TAG_HOME_FRAGMENT = "HomeFragment"
    const val TAG_HISTORY_FRAGMENT = "HistoryFragment"
    const val TAG_MONITOR_FRAGMENT = "MonitorFragment"
    const val TAG_TRANSACTION_LOG_ACTIVITY = "TransactionLogActivity"

    // ✅ BUNDLE KEYS - For fragment communication
    const val BUNDLE_RECEIPT_LIST = "bundle_receipt_list"
    const val BUNDLE_TRANSACTION_LOGS = "bundle_transaction_logs"
    const val BUNDLE_SEARCH_QUERY = "bundle_search_query"
    const val BUNDLE_FILTER_CRITERIA = "bundle_filter_criteria"

    // ✅ DEEP LINK PATTERNS - For future use
    const val DEEP_LINK_HOME = "gespay://home"
    const val DEEP_LINK_RECEIPT = "gespay://receipt/{ppid}"
    const val DEEP_LINK_TRANSACTION = "gespay://transaction/{ppid}"

    // ✅ TRANSITION ANIMATIONS - Consistent animations
    object Animations {
        const val ENTER_FROM_RIGHT = android.R.anim.slide_in_left
        const val EXIT_TO_LEFT = android.R.anim.slide_out_right
        const val ENTER_FROM_LEFT = android.R.anim.slide_in_left
        const val EXIT_TO_RIGHT = android.R.anim.slide_out_right
        const val FADE_IN = android.R.anim.fade_in
        const val FADE_OUT = android.R.anim.fade_out
    }

    // ✅ VALIDATION HELPERS
    fun isValidPpid(ppid: String?): Boolean {
        return !ppid.isNullOrBlank() && ppid.length >= 5
    }

    fun isValidReceiptRef(refNumber: String?): Boolean {
        return !refNumber.isNullOrBlank() && refNumber.length >= 3
    }

    // ✅ NAVIGATION HELPERS
    fun createTransactionLogBundle(ppid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_PPID, ppid)
        }
    }

    fun createReceiptBundle(receiptRef: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_RECEIPT_REF, receiptRef)
        }
    }

    // ✅ DEBUG HELPERS
    fun getNavigationDebugInfo(): String {
        return """
        NavigationConstants Debug Info:
        - Available Destinations: ${Destinations::class.java.declaredFields.size}
        - Available Actions: ${Actions::class.java.declaredFields.size}
        - Supported Args: ppid, receiptRef, noLoket (legacy)
        - Deep Links: Configured for receipt and transaction flows
        """.trimIndent()
    }
}

/**
 * ✅ PHASE 2: Extension functions for easier navigation
 */

// Navigation argument extensions
fun android.os.Bundle.getPpid(): String? = getString(NavigationConstants.ARG_PPID)
fun android.os.Bundle.getReceiptRef(): String? = getString(NavigationConstants.ARG_RECEIPT_REF)
fun android.os.Bundle.getNoLoket(): String? = getString(NavigationConstants.ARG_NO_LOKET) // Legacy

// Bundle creation extensions
fun String.toNavigationBundle(): android.os.Bundle {
    return android.os.Bundle().apply {
        putString(NavigationConstants.ARG_PPID, this@toNavigationBundle)
    }
}

// Validation extensions
fun String?.isValidNavigationId(): Boolean = NavigationConstants.isValidPpid(this)
fun String?.isValidReceiptReference(): Boolean = NavigationConstants.isValidReceiptRef(this)