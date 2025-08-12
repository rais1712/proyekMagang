package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class BlockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    // Diperbarui agar lebih jelas menggunakan noLoket
    operator fun invoke(noLoket: String) = repository.blockLoket(noLoket)
}