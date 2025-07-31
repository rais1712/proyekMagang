package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class GetLoketByPhoneUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(phoneNumber: String): NetworkResult<Loket> {
        if (phoneNumber.isBlank()) {
            return NetworkResult.Error(message = "Nomor telepon tidak boleh kosong")
        }

        // Format nomor telepon (hapus spasi dan karakter khusus)
        val formattedPhone = phoneNumber.replace(Regex("[^0-9]"), "")

        // Validasi format nomor Indonesia
        if (!formattedPhone.matches(Regex("^(\\+62|62|0)[8][1-9][0-9]{6,9}\$"))) {
            return NetworkResult.Error(
                message = "Format nomor telepon tidak valid. Gunakan format Indonesia: 08xx-xxxx-xxxx"
            )
        }

        // Konversi ke format 62xxx
        val normalizedPhone = when {
            formattedPhone.startsWith("0") -> "62${formattedPhone.substring(1)}"
            formattedPhone.startsWith("62") -> formattedPhone
            formattedPhone.startsWith("+62") -> formattedPhone.substring(1)
            else -> "62$formattedPhone"
        }

        return repository.getLoketByPhone(normalizedPhone)
    }
}
