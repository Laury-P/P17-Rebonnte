package com.openclassrooms.rebonnte.core.domain.repository

import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun getListAisles() : Flow<List<Aisle>>

    fun getListAllMedicine() : Flow<List<Medicine>>

    fun getListMedicineByAisleId(aisleId : String) : Flow<List<Medicine>>

    fun getMedicineDetailById(medicineId: String) : Flow<Medicine>

    suspend fun addAisle(aisle: Aisle) : Result<Unit>

    suspend fun saveMedicine(medicine: Medicine, history: History) : Result<Unit>

    suspend fun deleteMedicine(medicineId : String) : Result<Unit>

    fun generateAisleId() : String

    fun generateMedicineId() : String

}