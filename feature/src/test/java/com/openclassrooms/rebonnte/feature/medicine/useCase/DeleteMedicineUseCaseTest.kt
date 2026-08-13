package com.openclassrooms.rebonnte.feature.medicine.useCase

import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeleteMedicineUseCaseTest {

    private val medicineRepository = mockk<MedicineRepository>()
    private val deleteMedicineUseCase = DeleteMedicineUseCase(medicineRepository)

    @Test
    fun `invoke should call repository deleteMedicine and return Success`() = runTest {
        // Given
        val medicineId = "med_1"
        coEvery { medicineRepository.deleteMedicine(medicineId) } returns Result.success(Unit)

        // When
        val result = deleteMedicineUseCase(medicineId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { medicineRepository.deleteMedicine(medicineId) }
    }

    @Test
    fun `invoke should return Failure when repository returns failure`() = runTest {
        // Given
        val medicineId = "med_1"
        val exception = Exception("Database error")
        coEvery { medicineRepository.deleteMedicine(medicineId) } returns Result.failure(exception)

        // When
        val result = deleteMedicineUseCase(medicineId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { medicineRepository.deleteMedicine(medicineId) }
    }

    @Test
    fun `invoke should return Failure when repository throws exception`() = runTest {
        // Given
        val medicineId = "med_1"
        val exception = Exception("Unexpected error")
        coEvery { medicineRepository.deleteMedicine(medicineId) } throws exception

        // When
        val result = deleteMedicineUseCase(medicineId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { medicineRepository.deleteMedicine(medicineId) }
    }
}
