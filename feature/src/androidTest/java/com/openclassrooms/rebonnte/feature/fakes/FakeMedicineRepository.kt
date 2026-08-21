package com.openclassrooms.rebonnte.feature.fakes

import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.MedicineSortOption
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMedicineRepository : MedicineRepository {

    private val aislesFlow = MutableStateFlow<List<Aisle>>(emptyList())
    private val medicinesFlow = MutableStateFlow<List<Medicine>>(emptyList())

    var shouldReturnError = false

    override fun getListAisles(): Flow<List<Aisle>> = aislesFlow

    override fun getListAllMedicine(sortOption: MedicineSortOption): Flow<List<Medicine>> {
        return medicinesFlow.map { list ->
            when (sortOption) {
                MedicineSortOption.NAME -> list.sortedBy { it.name }
                MedicineSortOption.STOCK -> list.sortedBy { it.stock }
                else -> list
            }
        }
    }

    override fun getListMedicineByAisleId(aisleId: String): Flow<List<Medicine>> {
        return medicinesFlow.map { list -> list.filter { it.aisleId == aisleId } }
    }

    override fun getAisleNameById(aisleId: String): Flow<Aisle> {
        return aislesFlow.map { list -> list.first { it.aisleId == aisleId } }
    }

    override fun getMedicineDetailById(medicineId: String): Flow<Medicine> {
        return medicinesFlow.map { list -> list.first { it.medicineId == medicineId } }
    }

    override suspend fun addAisle(aisle: Aisle): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Fake Error"))
        aislesFlow.update { it + aisle }
        return Result.success(Unit)
    }

    override suspend fun saveMedicine(medicine: Medicine, history: History): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Fake Error"))
        medicinesFlow.update { list ->
            val index = list.indexOfFirst { it.medicineId == medicine.medicineId }
            if (index != -1) {
                list.toMutableList().apply { set(index, medicine) }.toList()
            } else {
                list + medicine
            }
        }
        return Result.success(Unit)
    }

    override suspend fun deleteMedicine(medicineId: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Fake Error"))
        medicinesFlow.update { list -> list.filterNot { it.medicineId == medicineId } }
        return Result.success(Unit)
    }

    override fun generateAisleId(): String = "aisle_${System.currentTimeMillis()}"

    override fun generateMedicineId(): String = "med_${System.currentTimeMillis()}"
    
    // Helper to seed data
    fun seedAisles(aisles: List<Aisle>) {
        aislesFlow.value = aisles
    }

    fun seedMedicines(medicines: List<Medicine>) {
        medicinesFlow.value = medicines
    }
}
