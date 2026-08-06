package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

class GetMedicineDetailUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val userRepository: UserRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(medicineId: String): Flow<Medicine> {
        return medicineRepository.getMedicineDetailById(medicineId).flatMapLatest { medicine ->
            medicineRepository.getAisleNameById(medicine.aisleId).map { aisle ->
                val historiesWithUsers = medicine.histories?.map { history ->
                    val user = userRepository.getUserById(history.userId)
                    history.copy(user = user)
                }
                medicine.copy(
                    aisleName = aisle.name,
                    histories = historiesWithUsers
                )
            }
        }
    }
}
