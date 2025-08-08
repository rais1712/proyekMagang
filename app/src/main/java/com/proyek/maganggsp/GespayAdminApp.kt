// File: app/src/main/java/com/proyek/maganggsp/GesPayAdminApp.kt
package com.proyek.maganggsp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Kelas Application dasar yang diperlukan untuk inisialisasi Hilt.
 * Anotasi @HiltAndroidApp akan memicu pembuatan kode Hilt.
 */
@HiltAndroidApp
class GesPayAdminApp : Application()