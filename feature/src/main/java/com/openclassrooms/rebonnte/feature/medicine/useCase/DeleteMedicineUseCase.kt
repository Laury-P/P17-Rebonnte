package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import jakarta.inject.Inject

class DeleteMedicineUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(medicineId : String) : Result<Unit> = runCatching {
        medicineRepository.deleteMedicine(medicineId).getOrThrow()
    }
}