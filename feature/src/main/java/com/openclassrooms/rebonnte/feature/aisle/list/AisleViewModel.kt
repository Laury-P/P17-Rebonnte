package com.openclassrooms.rebonnte.feature.aisle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AisleViewModel @Inject constructor(private val medicineRepository: MedicineRepository) :
    ViewModel() {

    var uiState: StateFlow<ListAislesState> = medicineRepository.getListAisles()
        .map { list ->
            ListAislesState.Success(list) as ListAislesState
        }
        .catch { error ->
            emit(ListAislesState.Error(error.message ?: "unkown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListAislesState.Loading
        )


    fun addAisle(title : String) {
        val id = medicineRepository.generateAisleId()
        val newAisle = Aisle(
            name = title,
            aisleId = id
        )
        viewModelScope.launch {
            medicineRepository.addAisle(newAisle)
                .onFailure {
                    //TODO Gerer l'erreur
                }
        }
    }
}

sealed interface ListAislesState {
    data object Loading : ListAislesState
    data class Success(val listAisle: List<Aisle>) : ListAislesState
    data class Error(val error: String) : ListAislesState
}