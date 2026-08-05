package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import jakarta.inject.Inject

class NewMedicineUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(name : String, aisleId:String, stock : Int) : Result<Unit> = runCatching {
        val userId = authRepository.getUserId() ?: throw(IllegalStateException("User not logged in"))
        val medicineId = medicineRepository.generateMedicineId()

        val initialHistory = History(
            userId = userId,
            medicineId = medicineId,
            timeStamp = System.currentTimeMillis(),
            details = "Création du médicament avec un stock initial de $stock"
        )

        val newMedicine = Medicine(
            medicineId = medicineId,
            name = name,
            stock = stock,
            aisleId = aisleId
        )

        medicineRepository.saveMedicine(medicine = newMedicine, history = initialHistory)
    }
}