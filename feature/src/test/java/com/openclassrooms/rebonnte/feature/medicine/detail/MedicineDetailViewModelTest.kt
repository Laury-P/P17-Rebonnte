package com.openclassrooms.rebonnte.feature.medicine.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.medicine.useCase.GetMedicineDetailUseCase
import com.openclassrooms.rebonnte.feature.medicine.useCase.UpdateStockUseCase
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class MedicineDetailViewModelTest {

    private val getMedicineDetailUseCase = mockk<GetMedicineDetailUseCase>()
    private val updateStockUseCase = mockk<UpdateStockUseCase>()
    private val medicineId = "med_1"

    private fun createViewModel(id: String? = medicineId): MedicineDetailViewModel {
        val savedStateHandle = if (id != null) SavedStateHandle(mapOf("medicineId" to id))
        else SavedStateHandle()
        return MedicineDetailViewModel(getMedicineDetailUseCase, savedStateHandle, updateStockUseCase)
    }

    @Test
    fun `should emit Success when use case returns medicine`() = runTest {
        // Given
        val medicine = Medicine(medicineId = medicineId, name = "Doliprane", aisleName = "Cardiologie")
        every { getMedicineDetailUseCase(medicineId) } returns flowOf(medicine)

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem() as UiState.Success
            assertEquals("Cardiologie", state.medicine.aisleName)
            assertEquals("Doliprane", state.medicine.name)
        }
    }

    @Test
    fun `should emit Error when medicineId is missing in navigation`() = runTest {
        // When
        val viewModel = createViewModel(id = null)

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as UiState.Error
            assertEquals("Medicine Id missing", state.error)
        }
    }

    @Test
    fun `should emit Error when use case throws exception`() = runTest {
        // Given
        every { getMedicineDetailUseCase(medicineId) } returns flow { throw Exception("Network Fail") }

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem() as UiState.Error
            assertEquals("Network Fail", state.error)
        }
    }

    @Test
    fun `updateStock should update operationState to Success on success`() = runTest {
        // Given
        val medicine = Medicine(medicineId = medicineId, name = "Doliprane")
        coEvery { updateStockUseCase(any(), any()) } returns Result.success(Unit)
        every { getMedicineDetailUseCase(medicineId) } returns flowOf(medicine)

        val viewModel = createViewModel()

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.updateStock(medicine, isIncrease = true)
            
            assertEquals(OperationState.Loading, awaitItem())
            assertEquals(OperationState.Success, awaitItem())
        }

        coVerify { updateStockUseCase(medicine, isIncrease = true) }
    }

    @Test
    fun `updateStock should update operationState to Error on failure`() = runTest {
        // Given
        val medicine = Medicine(medicineId = medicineId, name = "Doliprane")
        val errorMsg = "Update failed"
        coEvery { updateStockUseCase(any(), any()) } returns Result.failure(Exception(errorMsg))
        every { getMedicineDetailUseCase(medicineId) } returns flowOf(medicine)

        val viewModel = createViewModel()

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.updateStock(medicine, isIncrease = false)

            assertEquals(OperationState.Loading, awaitItem())
            val state = awaitItem() as OperationState.Error
            assertEquals(errorMsg, state.error)
        }

        coVerify { updateStockUseCase(medicine, isIncrease = false) }
    }
}
