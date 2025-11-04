package com.proyek.maganggsp.util

import android.content.Intent

object Extensions {
    fun String.extractPpidSafely(fallback: String = ""): String {
        return if (this.isNotBlank()) this else fallback
    }
}

fun Intent.extractPpid(): String {
    return getStringExtra(NavigationConstants.ARG_PPID)
        ?: getBundleExtra("android:support:navigation:fragment:args")?.getString(NavigationConstants.ARG_PPID)
        ?: getStringExtra("ppid")
        ?: ""
}