package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class GetLoketDetailUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    // Diperbarui untuk menggunakan noLoket
    operator fun invoke(noLoket: String) = repository.getLoketDetail(noLoket)
}