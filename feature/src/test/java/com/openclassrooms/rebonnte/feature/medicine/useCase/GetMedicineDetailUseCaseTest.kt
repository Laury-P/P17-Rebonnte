package com.openclassrooms.rebonnte.feature.medicine.useCase

import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.History
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.User
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetMedicineDetailUseCaseTest {

    private val medicineRepository = mockk<MedicineRepository>()
    private val userRepository = mockk<UserRepository>()
    private val useCase = GetMedicineDetailUseCase(medicineRepository, userRepository)

    @Test
    fun `should return medicine with enriched aisle name and history users`() = runTest {
        // Given
        val medicineId = "med_1"
        val userId = "user_1"
        val history = History(medicineId = medicineId, userId = userId, details = "Test")
        val medicine = Medicine(
            medicineId = medicineId,
            name = "Doliprane",
            aisleId = "A1",
            histories = listOf(history)
        )
        val aisle = Aisle(aisleId = "A1", name = "Pharmacie")
        val user = User(id = userId, name = "John Doe", email = "john@doe.com")

        every { medicineRepository.getMedicineDetailById(medicineId) } returns flowOf(medicine)
        every { medicineRepository.getAisleNameById("A1") } returns flowOf(aisle)
        coEvery { userRepository.getUserById(userId) } returns user

        // When & Then
        useCase(medicineId).test {
            val result = awaitItem()
            assertEquals("Pharmacie", result.aisleName)
            assertNotNull(result.histories)
            assertEquals("John Doe", result.histories?.first()?.user?.name)
            awaitComplete()
        }
    }

    @Test
    fun `should throw exception when medicine repository fails`() = runTest {
        // Given
        val medicineId = "med_1"
        val errorMsg = "Medicine not found"
        every { medicineRepository.getMedicineDetailById(medicineId) } returns kotlinx.coroutines.flow.flow {
            throw Exception(errorMsg)
        }

        // When & Then
        useCase(medicineId).test {
            val error = awaitError()
            assertEquals(errorMsg, error.message)
        }
    }

    @Test
    fun `should throw exception when aisle repository fails`() = runTest {
        // Given
        val medicineId = "med_1"
        val medicine = Medicine(medicineId = medicineId, aisleId = "A1")
        every { medicineRepository.getMedicineDetailById(medicineId) } returns flowOf(medicine)
        every { medicineRepository.getAisleNameById("A1") } returns kotlinx.coroutines.flow.flow {
            throw Exception("Aisle error")
        }

        // When & Then
        useCase(medicineId).test {
            val error = awaitError()
            assertEquals("Aisle error", error.message)
        }
    }

    @Test
    fun `should return medicine with null user if user is not found in database`() = runTest {
        // Given
        val medicineId = "med_1"
        val userId = "unknown_user"
        val history = History(userId = userId)
        val medicine = Medicine(medicineId = medicineId, aisleId = "A1", histories = listOf(history))
        val aisle = Aisle(aisleId = "A1", name = "Test")

        every { medicineRepository.getMedicineDetailById(medicineId) } returns flowOf(medicine)
        every { medicineRepository.getAisleNameById("A1") } returns flowOf(aisle)
        coEvery { userRepository.getUserById(userId) } returns null // Simule user introuvable

        // When & Then
        useCase(medicineId).test {
            val result = awaitItem()
            assertEquals(null, result.histories?.first()?.user)
            awaitComplete()
        }
    }
}
