// File: app/src/main/java/com/proyek/maganggsp/util/NavigationConstants.kt - STREAMLINED
package com.proyek.maganggsp.util

/**
 * STREAMLINED: Navigation constants dengan PPID focus
 * Eliminates complex multi-argument types, focuses on PPID as primary key
 */
object NavigationConstants {

    // PRIMARY NAVIGATION ARGUMENT - PPID Only
    const val ARG_PPID = "ppid"

    // LEGACY SUPPORT - For backward compatibility only
    const val ARG_NO_LOKET = "noLoket"
    const val ARG_CURRENT_PPID = "currentPpid"

    // SHARED PREFERENCES KEYS
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_ADMIN_EMAIL = "admin_email"

    // NAVIGATION HELPERS - PPID focused
    fun createDetailLoketBundle(ppid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_PPID, ppid)
            android.util.Log.d("NavigationConstants", "Created bundle dengan ppid: $ppid")
        }
    }

    fun createTransactionLogBundle(ppid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_PPID, ppid)
            android.util.Log.d("NavigationConstants", "Created TransactionLog bundle dengan ppid: $ppid")
        }
    }

    fun createUpdateProfileBundle(currentPpid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString(ARG_CURRENT_PPID, currentPpid)
        }
    }

    // VALIDATION
    fun isValidPpid(ppid: String?): Boolean {
        return !ppid.isNullOrBlank() && ppid.length >= 5
    }
}

// Navigation argument extensions - PPID focused
fun android.os.Bundle.getPpid(): String? = getString(NavigationConstants.ARG_PPID)
fun android.os.Bundle.getCurrentPpid(): String? = getString(NavigationConstants.ARG_CURRENT_PPID)

// Bundle creation extensions - PPID focused
fun String.toDetailLoketBundle(): android.os.Bundle {
    return NavigationConstants.createDetailLoketBundle(this)
}

fun String.toTransactionLogBundle(): android.os.Bundle {
    return NavigationConstants.createTransactionLogBundle(this)
}

// Safe PPID extraction
fun String?.extractPpidSafely(fallbackPpid: String = "PIDLKTD0025"): String {
    return when {
        NavigationConstants.isValidPpid(this) -> this!!
        !this.isNullOrBlank() && this.length > 5 -> this
        else -> fallbackPpid
    }.also { result ->
        android.util.Log.d("NavigationConstants", "Extracted ppid: $result from input: $this")
    }
}