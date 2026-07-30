package com.openclassrooms.rebonnte.feature.aisle.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class AisleDetailViewModelTest {

    private val repository = mockk<MedicineRepository>()
    private val aisleId = "aisle_1"

    private fun createViewModel(id: String? = aisleId): AisleDetailViewModel {
        val savedStateHandle = if (id != null) SavedStateHandle(mapOf("aisleId" to id)) 
                               else SavedStateHandle()
        return AisleDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `should emit Success with combined data when ID is valid`() = runTest {
        // Given
        val targetAisle = Aisle(aisleId, "Cardio")
        val medicines = listOf(Medicine("med1", "Doliprane", aisleId = aisleId))
        
        every { repository.getAisleNameById(aisleId) } returns flowOf(targetAisle)
        every { repository.getListMedicineByAisleId(aisleId) } returns flowOf(medicines)

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem() as UiState.Success
            assertEquals(targetAisle, state.aisle)
            assertEquals(medicines, state.medicines)
        }
    }

    @Test
    fun `should emit Error when aisleId is missing in navigation`() = runTest {
        // When
        val viewModel = createViewModel(id = null)

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as UiState.Error
            assertEquals("ID de rayon manquant", state.error)
        }
    }

    @Test
    fun `should emit Error when repository throws exception`() = runTest {
        // Given
        every { repository.getAisleNameById(aisleId) } returns flow { throw Exception("Network Fail") }
        every { repository.getListMedicineByAisleId(aisleId) } returns flowOf(emptyList())

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem() as UiState.Error
            assertEquals("Network Fail", state.error)
        }
    }
}
