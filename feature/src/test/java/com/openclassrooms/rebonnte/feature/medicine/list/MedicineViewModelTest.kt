package com.openclassrooms.rebonnte.feature.medicine.list

import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.feature.medicine.useCase.NewMedicineUseCase
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class MedicineViewModelTest {

    private val repository = mockk<MedicineRepository>()
    private val newMedicineUseCase = mockk<NewMedicineUseCase>()
    private lateinit var viewModel: MedicineViewModel

    private val testMedicines = listOf(
        Medicine(medicineId = "1", name = "Doliprane", stock = 50, aisleId = "A1"),
        Medicine(medicineId = "2", name = "Aspégic", stock = 10, aisleId = "A1"),
        Medicine(medicineId = "3", name = "Zyrtec", stock = 100, aisleId = "A2")
    )

    @BeforeEach
    fun setup() {
        every { repository.getListAllMedicine() } returns flowOf(testMedicines)
        every { repository.getListAisles() } returns flowOf(emptyList())
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit Success when repository returns medicines`() = runTest {
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            
            // Advance time for debounce in combine
            advanceTimeBy(300)
            
            val state = awaitItem() as ListMedicinesState.Success
            assertEquals(testMedicines, state.listMedicine)
        }
    }

    @Test
    fun `should filter medicines by name with debounce`() = runTest {
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            assertEquals(testMedicines, (awaitItem() as ListMedicinesState.Success).listMedicine)

            // When searching
            viewModel.filterByName("Doli")
            
            // Wait for debounce (200ms)
            advanceTimeBy(300)

            val state = awaitItem() as ListMedicinesState.Success
            assertEquals(1, state.listMedicine.size)
            assertEquals("Doliprane", state.listMedicine[0].name)
        }
    }

    @Test
    fun `should sort medicines by name`() = runTest {
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            advanceTimeBy(300)
            awaitItem() // Skip initial success

            // When sorting by name
            viewModel.sortByName()
            advanceTimeBy(300)

            val state = awaitItem() as ListMedicinesState.Success
            assertEquals("Aspégic", state.listMedicine[0].name)
            assertEquals("Doliprane", state.listMedicine[1].name)
            assertEquals("Zyrtec", state.listMedicine[2].name)
        }
    }

    @Test
    fun `should sort medicines by stock`() = runTest {
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            advanceTimeBy(300)
            awaitItem() // Skip initial success

            // When sorting by stock
            viewModel.sortByStock()
            advanceTimeBy(300)

            val state = awaitItem() as ListMedicinesState.Success
            assertEquals("Aspégic", state.listMedicine[0].name) // 10
            assertEquals("Doliprane", state.listMedicine[1].name) // 50
            assertEquals("Zyrtec", state.listMedicine[2].name) // 100
        }
    }

    @Test
    fun `should emit Error when repository fails`() = runTest {
        every { repository.getListAllMedicine() } returns flow { throw Exception("Firestore Error") }
        
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            
            advanceTimeBy(300)
            
            val state = awaitItem() as ListMedicinesState.Error
            assertEquals("Firestore Error", state.error)
        }
    }

    @Test
    fun `retry should reload medicines`() = runTest {
        var callCount = 0
        every { repository.getListAllMedicine() } answers {
            callCount++
            if (callCount == 1) flow { throw Exception("Fail") }
            else flowOf(testMedicines)
        }

        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.uiState.test {
            assertEquals(ListMedicinesState.Loading, awaitItem())
            
            advanceTimeBy(300)
            
            assertTrue(awaitItem() is ListMedicinesState.Error)

            // When retrying
            viewModel.retry()
            
            advanceTimeBy(300)

            assertTrue(awaitItem() is ListMedicinesState.Success)
        }
    }

    @Test
    fun `aisles should load correctly`() = runTest {
        val testAisles = listOf(Aisle("A1", "Cardio"), Aisle("A2", "Ortho"))
        every { repository.getListAisles() } returns flowOf(testAisles)

        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.aisles.test {
            assertEquals(emptyList<Aisle>(), awaitItem()) // Initial value
            assertEquals(testAisles, awaitItem())
        }
    }

    @Test
    fun `addMedicine should update operationState to Success on success`() = runTest {
        coEvery { newMedicineUseCase(any(), any(), any()) } returns Result.success(Unit)
        
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When adding medicine
            viewModel.addMedicine("Paracetamol", 20, "A1")

            assertEquals(OperationState.Loading, awaitItem())
            assertEquals(OperationState.Success, awaitItem())
        }

        coVerify { newMedicineUseCase("Paracetamol", "A1", 20) }
    }

    @Test
    fun `addMedicine should update operationState to Error on failure`() = runTest {
        coEvery { newMedicineUseCase(any(), any(), any()) } returns Result.failure(Exception("Creation Error"))
        
        viewModel = MedicineViewModel(repository, newMedicineUseCase)

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When adding medicine
            viewModel.addMedicine("Paracetamol", 20, "A1")

            assertEquals(OperationState.Loading, awaitItem())
            val state = awaitItem() as OperationState.Error
            assertEquals("Creation Error", state.error)
        }
    }
}
