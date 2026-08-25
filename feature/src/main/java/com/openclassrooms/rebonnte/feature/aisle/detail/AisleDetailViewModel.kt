package com.openclassrooms.rebonnte.feature.aisle.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.util.StringProvider
import com.openclassrooms.rebonnte.feature.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AisleDetailViewModel @Inject constructor(
    medicineRepository: MedicineRepository,
    savedStateHandle: SavedStateHandle,
    stringProvider: StringProvider
) :
    ViewModel() {

    private val aisleId: String? = savedStateHandle["aisleId"]

    val uiState: StateFlow<UiState> = if (aisleId != null) {
        combine(
            medicineRepository.getAisleNameById(aisleId),
            medicineRepository.getListMedicineByAisleId(aisleId)
        ) { aisle, medicines ->
            UiState.Success(aisle = aisle, medicines = medicines) as UiState
        }
            .catch { emit(UiState.Error(it.message ?: stringProvider.getString(R.string.error_unknown))) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
    } else {
        MutableStateFlow(UiState.Error(stringProvider.getString(R.string.error_aisle_id_missing)))
    }


}

sealed interface UiState {
    data object Loading : UiState
    data class Success(val aisle: Aisle, val medicines: List<Medicine>) : UiState
    data class Error(val error: String) : UiState
}

