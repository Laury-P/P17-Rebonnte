package com.openclassrooms.rebonnte.feature.medicine.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.feature.medicine.useCase.UpdateStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    savedStateHandle: SavedStateHandle,
    private val updateStockUseCase: UpdateStockUseCase
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState : StateFlow<UiState> = if (medicineId != null) {
        medicineRepository.getMedicineDetailById(medicineId)
            .flatMapLatest { medicine ->
                medicineRepository.getAisleNameById(medicine.aisleId).map { aisle ->
                    UiState.Success(medicine.copy(aisleName = aisle.name)) as UiState
                }
            }
            .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
    } else {
        MutableStateFlow(UiState.Error("Medicine Id missing"))
    }

    fun updateStock(medicine : Medicine, isIncrease : Boolean) {
        viewModelScope.launch {
            updateStockUseCase(medicine, isIncrease)
                .onFailure { Log.d("DEBUG_UPDATE","Failed update: ${it.message}" ) }
                .onSuccess { Log.d("DEBUG_UPDATE", "Success update") }
        }
    }
}

sealed interface UiState {
    data object Loading : UiState
    data class Success (val medicine : Medicine) : UiState
    data class Error(val error: String) : UiState
}