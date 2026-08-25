package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.model.Medicine
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
class UpdateStockUseCaseTest {

    private val medicineRepository = mockk<MedicineRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = UpdateStockUseCase(medicineRepository, authRepository)

    @Test
    fun `should increase stock and save successfully`() = runTest {
        // Given
        val userId = "user_123"
        val medicine = Medicine(medicineId = "med_1", name = "Doliprane", stock = 5)
        every { authRepository.getUserId() } returns userId
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.success(Unit)

        // When
        val result = useCase(medicine, isIncrease = true)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            medicineRepository.saveMedicine(
                match { it.stock == 6 },
                match { it.userId == userId && it.details.contains("New stock: 6") }
            )
        }
    }

    @Test
    fun `should decrease stock and save successfully`() = runTest {
        // Given
        val userId = "user_123"
        val medicine = Medicine(medicineId = "med_1", name = "Doliprane", stock = 5)
        every { authRepository.getUserId() } returns userId
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.success(Unit)

        // When
        val result = useCase(medicine, isIncrease = false)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            medicineRepository.saveMedicine(
                match { it.stock == 4 },
                match { it.userId == userId && it.details.contains("New stock: 4") }
            )
        }
    }

    @Test
    fun `should not decrease stock below zero`() = runTest {
        // Given
        val userId = "user_123"
        val medicine = Medicine(medicineId = "med_1", name = "Doliprane", stock = 0)
        every { authRepository.getUserId() } returns userId
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.success(Unit)

        // When
        val result = useCase(medicine, isIncrease = false)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            medicineRepository.saveMedicine(
                match { it.stock == 0 },
                any()
            )
        }
    }

    @Test
    fun `should return failure when user is not logged in`() = runTest {
        // Given
        val medicine = Medicine(medicineId = "med_1", stock = 5)
        every { authRepository.getUserId() } returns null

        // When
        val result = useCase(medicine, isIncrease = true)

        // Then
        assertTrue(result.isFailure)
        assertEquals("User not logged in", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should return failure when repository fails`() = runTest {
        // Given
        val userId = "user_123"
        val medicine = Medicine(medicineId = "med_1", stock = 5)
        every { authRepository.getUserId() } returns userId
        coEvery { medicineRepository.saveMedicine(any(), any()) } returns Result.failure(Exception("Update failed"))

        // When
        val result = useCase(medicine, isIncrease = true)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Update failed", result.exceptionOrNull()?.message)
    }
}
