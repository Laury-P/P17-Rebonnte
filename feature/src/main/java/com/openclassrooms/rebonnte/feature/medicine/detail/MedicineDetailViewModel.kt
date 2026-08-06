package com.openclassrooms.rebonnte.feature.medicine.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.medicine.useCase.GetMedicineDetailUseCase
import com.openclassrooms.rebonnte.feature.medicine.useCase.UpdateStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    getMedicineDetailUseCase: GetMedicineDetailUseCase,
    savedStateHandle: SavedStateHandle,
    private val updateStockUseCase: UpdateStockUseCase
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState = _operationState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState : StateFlow<UiState> = if (medicineId != null) {
        getMedicineDetailUseCase(medicineId)
            .map { medicine ->
                UiState.Success(medicine) as UiState
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
            _operationState.value = OperationState.Loading
            updateStockUseCase(medicine, isIncrease)
                .onFailure { _operationState.value = OperationState.Error(it.message ?: "Unknown error") }
                .onSuccess { _operationState.value = OperationState.Success }
        }
    }
}

sealed interface UiState {
    data object Loading : UiState
    data class Success (val medicine : Medicine) : UiState
    data class Error(val error: String) : UiState
}