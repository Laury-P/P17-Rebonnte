package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import jakarta.inject.Inject

class UpdateStockUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(medicine : Medicine, isIncrease : Boolean) : Result<Unit> = runCatching{
        val userId = authRepository.getUserId() ?: throw(IllegalStateException("User not logged in"))
        val newStock = if(isIncrease) medicine.stock + 1 else (medicine.stock - 1).coerceAtLeast(0)
        val updateHistory = History(
            userId = userId,
            medicineId = medicine.medicineId,
            timeStamp = System.currentTimeMillis(),
            details = "Stock update. New stock: $newStock"
        )

        val updatedMedicine = medicine.copy(stock = newStock)

        medicineRepository.saveMedicine(medicine = updatedMedicine, history = updateHistory).getOrThrow()
    }
}