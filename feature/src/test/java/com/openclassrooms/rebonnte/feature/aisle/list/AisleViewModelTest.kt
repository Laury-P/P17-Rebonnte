package com.openclassrooms.rebonnte.feature.aisle.list

import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class AisleViewModelTest {

    private val repository = mockk<MedicineRepository>()
    private lateinit var viewModel: AisleViewModel

    @Test
    fun `initial state should be Loading`() = runTest {
        // Given
        every { repository.getListAisles() } returns flowOf(emptyList())
        
        // When
        viewModel = AisleViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(ListAislesState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit Success when repository returns data`() = runTest {
        // Given
        val aisles = listOf(Aisle("1", "Cardio"), Aisle("2", "Ortho"))
        every { repository.getListAisles() } returns flowOf(aisles)

        // When
        viewModel = AisleViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(ListAislesState.Loading, awaitItem())
            val state = awaitItem() as ListAislesState.Success
            assertEquals(aisles, state.listAisle)
        }
    }

    @Test
    fun `should emit Error when repository fails`() = runTest {
        // Given
        every { repository.getListAisles() } returns flow { throw Exception("Firestore Error") }

        // When
        viewModel = AisleViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(ListAislesState.Loading, awaitItem())
            val state = awaitItem() as ListAislesState.Error
            assertEquals("Firestore Error", state.error)
        }
    }

    @Test
    fun `retry should trigger repository call again`() = runTest {
        // Given
        var callCount = 0
        every { repository.getListAisles() } answers {
            callCount++
            if (callCount == 1) flow { throw Exception("Fail") }
            else flowOf(listOf(Aisle("Succes", "1")))
        }

        viewModel = AisleViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ListAislesState.Loading, awaitItem())
            assertTrue(awaitItem() is ListAislesState.Error)
            
            // When
            viewModel.retry()
            
            // Then
            assertTrue(awaitItem() is ListAislesState.Success)
        }
    }

    @Test
    fun `addAisle should update operationState to Success when repository succeeds`() = runTest {
        // Given
        val title = "Neurologie"
        val generatedId = "id_123"
        every { repository.getListAisles() } returns flowOf(emptyList())
        every { repository.generateAisleId() } returns generatedId
        coEvery { repository.addAisle(any()) } returns Result.success(Unit)

        viewModel = AisleViewModel(repository)

        // Then
        viewModel.operationState.test {
            assertEquals(com.openclassrooms.rebonnte.core.domain.model.OperationState.Idle, awaitItem())
            
            // When
            viewModel.addAisle(title)
            
            assertEquals(com.openclassrooms.rebonnte.core.domain.model.OperationState.Loading, awaitItem())
            assertEquals(com.openclassrooms.rebonnte.core.domain.model.OperationState.Success, awaitItem())
        }

        coVerify { 
            repository.addAisle(match { it.name == title && it.aisleId == generatedId }) 
        }
    }

    @Test
    fun `addAisle should update operationState to Error when repository fails`() = runTest {
        // Given
        val title = "Neurologie"
        val errorMsg = "Add Failed"
        every { repository.getListAisles() } returns flowOf(emptyList())
        every { repository.generateAisleId() } returns "id_123"
        coEvery { repository.addAisle(any()) } returns Result.failure(Exception(errorMsg))

        viewModel = AisleViewModel(repository)

        // Then
        viewModel.operationState.test {
            assertEquals(com.openclassrooms.rebonnte.core.domain.model.OperationState.Idle, awaitItem())
            
            // When
            viewModel.addAisle(title)
            
            assertEquals(com.openclassrooms.rebonnte.core.domain.model.OperationState.Loading, awaitItem())
            val state = awaitItem() as com.openclassrooms.rebonnte.core.domain.model.OperationState.Error
            assertEquals("Add Failed", state.error)
        }
    }
}
