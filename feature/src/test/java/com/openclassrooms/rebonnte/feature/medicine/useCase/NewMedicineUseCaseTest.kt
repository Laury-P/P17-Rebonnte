package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewMedicineUseCaseTest {

    private val medicineRepository = mockk<MedicineRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = NewMedicineUseCase(medicineRepository, authRepository)

    @Test
    fun `should save medicine successfully when user is logged in`() = runTest {
        // Given
        val name = "Doliprane"
        val aisleId = "A1"
        val stock = 10
        val userId = "user_123"
        val medicineId = "med_456"

        every { authRepository.getUserId() } returns userId
        every { medicineRepository.generateMedicineId() } returns medicineId
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.success(Unit)

        // When
        val result = useCase(name, aisleId, stock)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            medicineRepository.saveMedicine(
                match { it.name == name && it.medicineId == medicineId && it.stock == stock && it.aisleId == aisleId },
                match { it.userId == userId && it.medicineId == medicineId }
            )
        }
    }

    @Test
    fun `should return failure when user is not logged in`() = runTest {
        // Given
        every { authRepository.getUserId() } returns null

        // When
        val result = useCase("Doliprane", "A1", 10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("User not logged in", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return failure when repository fails to save`() = runTest {
        // Given
        val userId = "user_123"
        every { authRepository.getUserId() } returns userId
        every { medicineRepository.generateMedicineId() } returns "med_1"
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.failure(Exception("DB Error"))

        // When
        val result = useCase("Doliprane", "A1", 10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("DB Error", result.exceptionOrNull()?.message)
    }
}
