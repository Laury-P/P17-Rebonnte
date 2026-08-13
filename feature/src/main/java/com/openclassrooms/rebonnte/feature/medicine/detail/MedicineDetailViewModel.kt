package com.openclassrooms.rebonnte.feature.medicine.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.feature.medicine.useCase.GetMedicineDetailUseCase
import com.openclassrooms.rebonnte.feature.medicine.useCase.UpdateStockUseCase
import com.openclassrooms.rebonnte.feature.medicine.useCase.DeleteMedicineUseCase
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
    private val updateStockUseCase: UpdateStockUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]

    private val _updateState = MutableStateFlow<OperationState>(OperationState.Idle)
    val updateState = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<OperationState>(OperationState.Idle)
    val deleteState = _deleteState.asStateFlow()

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
        _updateState.value = OperationState.Loading
        viewModelScope.launch {
            updateStockUseCase(medicine, isIncrease)
                .onFailure { _updateState.value = OperationState.Error(it.message ?: "Unknown error") }
                .onSuccess { _updateState.value = OperationState.Success }
        }
    }

    fun deleteMedicine() {
        _deleteState.value = OperationState.Loading
        viewModelScope.launch {
            medicineId?.let { id ->

                deleteMedicineUseCase(id)
                    .onFailure { _deleteState.value = OperationState.Error(it.message ?: "Unknown error while deleting") }
                    .onSuccess { _deleteState.value = OperationState.Success }
            }
        }
    }
}

sealed interface UiState {
    data object Loading : UiState
    data class Success (val medicine : Medicine) : UiState
    data class Error(val error: String) : UiState
}