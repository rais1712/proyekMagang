// File: app/src/main/java/com/proyek/maganggsp/data/source/local/LoketHistoryManager.kt - PPID SEARCH ENHANCED
package com.proyek.maganggsp.data.source.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.domain.model.LoketStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoketHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    companion object {
        private const val TAG = "LoketHistoryManager"
        private const val PREFS_NAME = "loket_history_prefs"
        private const val KEY_RECENT_HISTORY = "recent_history"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_PPID_INDEX = "ppid_search_index"
        private const val MAX_HISTORY_SIZE = 50
        private const val MAX_FAVORITES_SIZE = 20
    }

    private var _sharedPreferences: SharedPreferences? = null

    private val sharedPreferences: SharedPreferences
        get() {
            if (_sharedPreferences == null) {
                _sharedPreferences = createSharedPreferences()
            }
            return _sharedPreferences!!
        }

    private fun createSharedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create encrypted preferences, using regular", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Save loket to access history dengan frequency tracking
     */
    fun saveToHistory(loket: Loket) {
        try {
            val currentHistory = getRecentHistory().toMutableList()

            // Check if loket already exists in history
            val existingIndex = currentHistory.indexOfFirst { it.ppid == loket.ppid }

            if (existingIndex >= 0) {
                // Update existing entry
                val existing = currentHistory[existingIndex]
                val updated = existing.copy(
                    tanggalAkses = System.currentTimeMillis(),
                    jumlahAkses = existing.jumlahAkses + 1,
                    namaLoket = loket.namaLoket, // Update nama jika berubah
                    nomorHP = loket.nomorHP,
                    email = loket.email,
                    alamat = loket.alamat,
                    status = loket.status
                )
                currentHistory[existingIndex] = updated
            } else {
                // Add new entry
                val newHistory = LoketSearchHistory(
                    ppid = loket.ppid,
                    namaLoket = loket.namaLoket,
                    nomorHP = loket.nomorHP,
                    email = loket.email,
                    alamat = loket.alamat,
                    status = loket.status,
                    tanggalAkses = System.currentTimeMillis(),
                    jumlahAkses = 1
                )
                currentHistory.add(0, newHistory)
            }

            // Sort by access time (most recent first)
            currentHistory.sortByDescending { it.tanggalAkses }

            // Limit history size
            val trimmedHistory = currentHistory.take(MAX_HISTORY_SIZE)

            // Save to preferences
            val json = gson.toJson(ppidIndex)
            sharedPreferences.edit()
                .putString(KEY_PPID_INDEX, json)
                .apply()

        } catch (e: Exception) {
            Log.w(TAG, "Failed to update PPID search index", e)
        }
    }

    /**
     * Get statistics about history usage
     */
    fun getHistoryStats(): HistoryStats {
        return try {
            val history = getRecentHistory()
            val favorites = getFavoritePpids()

            HistoryStats(
                totalAccessed = history.size,
                totalFavorites = favorites.size,
                mostAccessedLoket = history.maxByOrNull { it.jumlahAkses },
                lastAccessTime = history.maxOfOrNull { it.tanggalAkses } ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get history stats", e)
            HistoryStats()
        }
    }

    /**
     * PPID Index Entry untuk efficient searching
     */
    private data class PpidIndexEntry(
        val ppid: String,
        val namaLoket: String,
        val accessCount: Int,
        val lastAccess: Long
    )

    /**
     * History Statistics
     */
    data class HistoryStats(
        val totalAccessed: Int = 0,
        val totalFavorites: Int = 0,
        val mostAccessedLoket: LoketSearchHistory? = null,
        val lastAccessTime: Long = 0L
    )

    /**
     * ENHANCED: Get suggestions berdasarkan PPID pattern
     */
    fun getPpidSuggestions(query: String, limit: Int = 5): List<String> {
        return try {
            if (query.length < 2) return emptyList()

            val history = getRecentHistory()
            val lowerQuery = query.lowercase()

            history.filter {
                it.ppid.lowercase().startsWith(lowerQuery)
            }
                .sortedByDescending { it.jumlahAkses }
                .take(limit)
                .map { it.ppid }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get PPID suggestions", e)
            emptyList()
        }
    }

    /**
     * ENHANCED: Bulk update lokets
     */
    fun bulkUpdateLokets(lokets: List<Loket>) {
        try {
            lokets.forEach { loket ->
                saveToHistory(loket)
            }
            Log.d(TAG, "Bulk updated ${lokets.size} lokets")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bulk update lokets", e)
        }
    }

    /**
     * DEBUG: Get debug info
     */
    fun getDebugInfo(): String {
        val stats = getHistoryStats()
        return """
        LoketHistoryManager Debug Info:
        - Total History: ${stats.totalAccessed}
        - Total Favorites: ${stats.totalFavorites}
        - Most Accessed: ${stats.mostAccessedLoket?.ppid ?: "None"}
        - Search Method: PPID-based pattern matching
        - Storage: ${if (_sharedPreferences != null) "Initialized" else "Not initialized"}
        """.trimIndent()
    }

    /**
     * Mengambil riwayat akses profile/loket terbaru
     */
    fun getRecentHistory(): List<LoketHistory> {
        val json = sharedPreferences.getString(KEY_RECENT_HISTORY, null)
        return if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<LoketHistory>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal parse recent history", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Mencari riwayat berdasarkan query PPID
     */
    fun searchByPpid(query: String): List<LoketHistory> {
        return getRecentHistory().filter { it.ppid.contains(query, ignoreCase = true) }
    }

    /**
     * ENHANCED: Search by PPID pattern
     */
    fun searchByPpid(ppidQuery: String): List<LoketSearchHistory> {
        return try {
            Log.d(TAG, "🔍 Searching for PPID pattern: $ppidQuery")

            val allHistory = getRecentHistory()
            val lowerQuery = ppidQuery.lowercase()

            val results = allHistory.filter { history ->
                // Match PPID pattern
                history.ppid.lowercase().contains(lowerQuery) ||
                        // Also match loket name for broader search
                        history.namaLoket.lowercase().contains(lowerQuery)
            }.sortedByDescending {
                // Prioritize exact PPID matches
                if (it.ppid.lowercase().startsWith(lowerQuery)) 2
                else if (it.ppid.lowercase().contains(lowerQuery)) 1
                else 0
            }

            Log.d(TAG, "🔍 PPID search results: ${results.size} matches")
            results

        } catch (e: Exception) {
            Log.e(TAG, "Failed to search by PPID", e)
            emptyList()
        }
    }

    /**
     * LEGACY: Search by phone number (kept for backward compatibility)
     */
    fun searchByPhoneNumber(phoneNumber: String): List<LoketSearchHistory> {
        return try {
            val allHistory = getRecentHistory()
            val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")

            allHistory.filter { history ->
                val cleanHistoryPhone = history.nomorHP.replace(Regex("[^0-9+]"), "")
                cleanHistoryPhone.contains(cleanPhone, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search by phone", e)
            emptyList()
        }
    }

    /**
     * Get recent loket access history
     */
    fun getRecentHistory(): List<LoketSearchHistory> {
        return try {
            val json = sharedPreferences.getString(KEY_RECENT_HISTORY, null)
            if (json != null) {
                val type = object : TypeToken<List<LoketSearchHistory>>() {}.type
                gson.fromJson<List<LoketSearchHistory>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get recent history", e)
            emptyList()
        }
    }

    /**
     * Get most frequently accessed lokets
     */
    fun getMostFrequentLokets(limit: Int = 10): List<LoketSearchHistory> {
        return try {
            getRecentHistory()
                .sortedByDescending { it.jumlahAkses }
                .take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get frequent lokets", e)
            emptyList()
        }
    }

    /**
     * ENHANCED: Update loket status in history
     */
    fun updateLoketStatus(ppid: String, newStatus: LoketStatus) {
        try {
            val currentHistory = getRecentHistory().toMutableList()
            val index = currentHistory.indexOfFirst { it.ppid == ppid }

            if (index >= 0) {
                val updated = currentHistory[index].copy(status = newStatus)
                currentHistory[index] = updated

                // Save updated history
                val json = gson.toJson(currentHistory)
                sharedPreferences.edit()
                    .putString(KEY_RECENT_HISTORY, json)
                    .apply()

                Log.d(TAG, "Updated status for $ppid to $newStatus")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update loket status", e)
        }
    }

    /**
     * ENHANCED: Get loket by exact PPID
     */
    fun getLoketByPpid(ppid: String): LoketSearchHistory? {
        return try {
            getRecentHistory().find { it.ppid.equals(ppid, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get loket by PPID", e)
            null
        }
    }

    /**
     * Clear all history
     */
    fun clearHistory() {
        try {
            sharedPreferences.edit()
                .remove(KEY_RECENT_HISTORY)
                .remove(KEY_PPID_INDEX)
                .apply()
            Log.d(TAG, "History cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear history", e)
        }
    }

    /**
     * Add PPID to favorites
     */
    fun addToFavorites(ppid: String) {
        try {
            val currentFavorites = getFavoritePpids().toMutableSet()
            currentFavorites.add(ppid)

            // Limit favorites size
            val trimmedFavorites = currentFavorites.take(MAX_FAVORITES_SIZE)

            val json = gson.toJson(trimmedFavorites)
            sharedPreferences.edit()
                .putString(KEY_FAVORITES, json)
                .apply()

            Log.d(TAG, "Added to favorites: $ppid")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to add to favorites", e)
        }
    }

    /**
     * Remove PPID from favorites
     */
    fun removeFromFavorites(ppid: String) {
        try {
            val currentFavorites = getFavoritePpids().toMutableSet()
            currentFavorites.remove(ppid)

            val json = gson.toJson(currentFavorites)
            sharedPreferences.edit()
                .putString(KEY_FAVORITES, json)
                .apply()

            Log.d(TAG, "Removed from favorites: $ppid")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove from favorites", e)
        }
    }

    /**
     * Check if PPID is in favorites
     */
    fun isFavorite(ppid: String): Boolean {
        return try {
            getFavoritePpids().contains(ppid)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check favorite status", e)
            false
        }
    }

    /**
     * Get favorite lokets with full data
     */
    fun getFavoriteLokets(): List<Loket> {
        return try {
            val favoritePpids = getFavoritePpids()
            val recentHistory = getRecentHistory()

            // Map favorites to Loket objects from history
            favoritePpids.mapNotNull { ppid ->
                val historyItem = recentHistory.find { it.ppid == ppid }
                historyItem?.let { history ->
                    Loket(
                        ppid = history.ppid,
                        namaLoket = history.namaLoket,
                        nomorHP = history.nomorHP,
                        alamat = history.alamat ?: "",
                        email = history.email ?: "",
                        status = history.status,
                        saldoTerakhir = 0L,
                        tanggalAkses = history.getFormattedTanggalAkses()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get favorite lokets", e)
            emptyList()
        }
    }

    /**
     * Get favorite PPIDs
     */
    private fun getFavoritePpids(): List<String> {
        return try {
            val json = sharedPreferences.getString(KEY_FAVORITES, null)
            if (json != null) {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get favorite PPIDs", e)
            emptyList()
        }
    }

    /**
     * Clear all favorites
     */
    fun clearFavorites() {
        try {
            sharedPreferences.edit()
                .remove(KEY_FAVORITES)
                .apply()
            Log.d(TAG, "Favorites cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear favorites", e)
        }
    }

    /**
     * ENHANCED: Create PPID search index untuk efficient searching
     */
    private fun updatePpidSearchIndex(history: List<LoketSearchHistory>) {
        try {
            val ppidIndex = history.associate {
                it.ppid.lowercase() to PpidIndexEntry(
                    ppid = it.ppid,
                    namaLoket = it.namaLoket,
                    accessCount = it.jumlahAkses,
                    lastAccess = it.tanggalAkses
                )
            }

            val json = gson.toJson(ppidIndex)
            sharedPreferences.edit()
                .putString(KEY_PPID_INDEX, json)
                .apply()

            Log.d(TAG, "PPID search index updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update PPID search index", e)
        }
    }
}

