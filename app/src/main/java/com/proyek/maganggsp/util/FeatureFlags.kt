// File: app/src/main/java/com/proyek/maganggsp/util/FeatureFlags.kt
package com.proyek.maganggsp.util

/**
 * SURGICAL CUTTING: Feature Flags untuk mengontrol fitur yang aktif/non-aktif
 *
 * PENDEKATAN: Conditional Compilation/Feature Flags
 * - Keep semua kode existing - tidak delete atau hapus apapun
 * - Add conditional logic - fitur-fitur yang belum siap di-"disable" secara runtime
 * - Use feature flags - boolean constants yang control mana fitur yang active
 *
 * TUJUAN: Fokus pada alur Login -> Home (sederhana) -> Logout -> Login
 */
object FeatureFlags {

    // ===== AUTH FLOW (ALWAYS ENABLED) =====
    const val ENABLE_LOGIN = true
    const val ENABLE_LOGOUT = true
    const val ENABLE_SESSION_MANAGEMENT = true

    // ===== HOME FRAGMENT FEATURES =====
    const val ENABLE_SEARCH_LOKET = true           // ✅ Core functionality - keep enabled
    const val ENABLE_RECENT_HISTORY = true         // ✅ Core functionality - keep enabled
    const val ENABLE_LOKET_DETAIL_NAVIGATION = true // ✅ Navigation to detail - keep enabled

    // ===== NAVIGATION FEATURES =====
    const val ENABLE_BOTTOM_NAVIGATION = true      // ✅ Keep for complete experience
    const val ENABLE_HISTORY_FRAGMENT = true       // ✅ Fragment sudah ada dan stable
    const val ENABLE_MONITOR_FRAGMENT = false      // ❌ Complex fitur, disable untuk focus

    // ===== DETAIL LOKET FEATURES (SURGICAL CUTTING) =====
    const val ENABLE_LOKET_DETAIL_VIEW = true      // ✅ Basic viewing - safe
    const val ENABLE_MUTATION_HISTORY = false      // ❌ Complex data loading - disable
    const val ENABLE_LOKET_ACTIONS = false         // ❌ Block/Unblock actions - disable
    const val ENABLE_FLAG_MANAGEMENT = false       // ❌ Flag actions - disable

    // ===== DATA LOADING FEATURES =====
    const val ENABLE_REAL_DATA_LOADING = true      // ✅ Real API calls for basic flow
    const val ENABLE_SHIMMER_LOADING = true        // ✅ Good UX - keep enabled
    const val ENABLE_PULL_TO_REFRESH = true        // ✅ Basic functionality - keep

    // ===== ADVANCED FEATURES (DISABLED) =====
    const val ENABLE_SEARCH_SUGGESTIONS = false    // ❌ Advanced UI - disable
    const val ENABLE_FAVORITES = false             // ❌ Additional feature - disable
    const val ENABLE_NOTIFICATIONS = false         // ❌ Complex feature - disable
    const val ENABLE_OFFLINE_MODE = false          // ❌ Complex caching - disable

    // ===== ERROR HANDLING & FALLBACKS =====
    const val ENABLE_DETAILED_ERROR_MESSAGES = true // ✅ Good for debugging
    const val ENABLE_FALLBACK_EMPTY_STATES = true   // ✅ Better UX
    const val ENABLE_MOCK_DATA_FALLBACK = false     // ❌ Use real data only

    // ===== DEBUG & DEVELOPMENT =====
    const val ENABLE_DEBUG_LOGGING = true          // ✅ Good for development
    const val ENABLE_SESSION_DEBUG_INFO = true     // ✅ Help troubleshoot auth issues
    const val ENABLE_NETWORK_LOGGING = true        // ✅ API debugging

    // ===== HELPER FUNCTIONS =====

    /**
     * Check if navigation should be limited to essential fragments only
     */
    fun isNavigationLimited(): Boolean {
        return !ENABLE_MONITOR_FRAGMENT
    }

    /**
     * Check if detail screen should be simplified (view-only mode)
     */
    fun isDetailScreenSimplified(): Boolean {
        return !ENABLE_LOKET_ACTIONS && !ENABLE_FLAG_MANAGEMENT
    }

    /**
     * Get enabled bottom navigation items
     */
    fun getEnabledBottomNavItems(): List<String> {
        val items = mutableListOf<String>()
        items.add("home") // Always enabled

        if (ENABLE_HISTORY_FRAGMENT) items.add("history")
        if (ENABLE_MONITOR_FRAGMENT) items.add("monitor")

        return items
    }

    /**
     * Check if mutation/transaction features should be shown
     */
    fun shouldShowMutationFeatures(): Boolean {
        return ENABLE_MUTATION_HISTORY && ENABLE_REAL_DATA_LOADING
    }

    /**
     * Check if action buttons should be shown in detail screen
     */
    fun shouldShowActionButtons(): Boolean {
        return ENABLE_LOKET_ACTIONS && !isDetailScreenSimplified()
    }

    /**
     * Get feature summary for debugging
     */
    fun getFeatureSummary(): String {
        return """
            🚩 FEATURE FLAGS SUMMARY:
            
            ✅ ENABLED FEATURES:
            - Login/Logout Flow: $ENABLE_LOGIN/$ENABLE_LOGOUT
            - Home Fragment: Basic functionality
            - Search Loket: $ENABLE_SEARCH_LOKET
            - Recent History: $ENABLE_RECENT_HISTORY
            - History Fragment: $ENABLE_HISTORY_FRAGMENT
            - Detail View (Read-only): $ENABLE_LOKET_DETAIL_VIEW
            
            ❌ DISABLED FEATURES:
            - Monitor Fragment: $ENABLE_MONITOR_FRAGMENT
            - Mutation History: $ENABLE_MUTATION_HISTORY
            - Loket Actions (Block/Unblock): $ENABLE_LOKET_ACTIONS
            - Flag Management: $ENABLE_FLAG_MANAGEMENT
            
            🎯 TARGET: Stable Login -> Home -> Logout flow
        """.trimIndent()
    }
}