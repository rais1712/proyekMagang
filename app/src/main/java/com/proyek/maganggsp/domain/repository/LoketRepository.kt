package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk semua aksi yang berhubungan dengan data loket dan mutasi.
 */
interface LoketRepository {

    // --- PERUBAHAN DI BARIS INI ---
    // Mengambil detail loket sekarang berdasarkan ID uniknya, bukan nomor telepon.
    fun getLoketDetail(idLoket: String): Flow<Resource<Loket>>

    fun getMutation(idLoket: String): Flow<Resource<List<Mutasi>>>

    fun searchLoket(query: String): Flow<Resource<List<Loket>>>

    // Fungsi-fungsi ini sudah benar menggunakan ID
    fun blockLoket(idLoket: String): Flow<Resource<Unit>>
    fun unblockLoket(idLoket: String): Flow<Resource<Unit>>
    fun flagMutation(idMutasi: String): Flow<Resource<Unit>>
    fun clearAllFlags(idLoket: String): Flow<Resource<Unit>>

    // Fungsi untuk mendapatkan daftar loket di halaman monitor
    fun getFlaggedLokets(): Flow<Resource<List<Loket>>>
    fun getBlockedLokets(): Flow<Resource<List<Loket>>>
}