package com.proyek.maganggsp.util

/**
 * Constants untuk navigation arguments dan keys
 * Mencegah typo dan memudahkan refactoring
 */
object NavigationConstants {

    // Navigation Arguments
    const val ARG_NO_LOKET = "noLoket"
    const val ARG_LOKET_DATA = "loketData"

    // Intent Extras
    const val EXTRA_NO_LOKET = "extra_no_loket"
    const val EXTRA_FROM_SEARCH = "extra_from_search"

    // SharedPreferences Keys
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"

    // Request Codes
    const val REQUEST_CODE_DETAIL = 1001
    const val REQUEST_CODE_LOGIN = 1002

    // Fragment Tags
    const val TAG_HOME_FRAGMENT = "HomeFragment"
    const val TAG_HISTORY_FRAGMENT = "HistoryFragment"
    const val TAG_MONITOR_FRAGMENT = "MonitorFragment"

    // Navigation Actions (untuk type safety)
    const val ACTION_HOME_TO_DETAIL = "action_homeFragment_to_detailLoketActivity"
    const val ACTION_HISTORY_TO_DETAIL = "action_historyFragment_to_detailLoketActivity"
    const val ACTION_MONITOR_TO_DETAIL = "action_monitorFragment_to_detailLoketActivity"
}