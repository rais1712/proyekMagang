// File: app/src/main/java/com/proyek/maganggsp/data/source/local/LoketHistoryManager.kt
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
     * Save loket to access history with frequency tracking
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
                    nomorHP = loket.nomorHP
                )
                currentHistory[existingIndex] = updated
            } else {
                // Add new entry
                val newHistory = LoketSearchHistory(
                    ppid = loket.ppid,
                    namaLoket = loket.namaLoket,
                    nomorHP = loket.nomorHP,
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
            val json = gson.toJson(trimmedHistory)
            sharedPreferences.edit()
                .putString(KEY_RECENT_HISTORY, json)
                .apply()

            Log.d(TAG, "Saved loket to history: ${loket.ppid}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to history", e)
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
     * Clear all history
     */
    fun clearHistory() {
        try {
            sharedPreferences.edit()
                .remove(KEY_RECENT_HISTORY)
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
                        alamat = "", // Not stored in history
                        email = "", // Not stored in history
                        status = com.proyek.maganggsp.domain.model.LoketStatus.NORMAL,
                        saldoTerakhir = 0L,
                        tanggalAkses = ""
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

    data class HistoryStats(
        val totalAccessed: Int = 0,
        val totalFavorites: Int = 0,
        val mostAccessedLoket: LoketSearchHistory? = null,
        val lastAccessTime: Long = 0L
    )
}