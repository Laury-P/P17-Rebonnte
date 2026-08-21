package com.openclassrooms.rebonnte.feature.medicine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.Medicine
import com.openclassrooms.rebonnte.core.domain.model.MedicineSortOption
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.util.StringProvider
import com.openclassrooms.rebonnte.feature.R
import com.openclassrooms.rebonnte.feature.medicine.useCase.NewMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val newMedicineUseCase: NewMedicineUseCase,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOption = MutableStateFlow(MedicineSortOption.NONE)

    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    val aisles: StateFlow<List<Aisle>> = medicineRepository.getListAisles()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState : StateFlow<OperationState> = _operationState

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<ListMedicinesState> = combine(
        refreshSignal,
        _sortOption
    ) { _, sortOption ->
        sortOption
    }.flatMapLatest { sortOption ->
        combine(
            medicineRepository.getListAllMedicine(sortOption),
            _searchQuery.debounce { query ->
                if (query.isEmpty()) 0L else 200L
            }
        ) { list, searchQuery ->
            val filteredList = list.filter { medicine ->
                if (searchQuery.isNotBlank()) {
                    medicine.name.contains(searchQuery, ignoreCase = true)
                } else true
            }
            ListMedicinesState.Success(listMedicine = filteredList) as ListMedicinesState
        }.catch { error ->
            emit(ListMedicinesState.Error(error.message ?: stringProvider.getString(R.string.error_medicine_load_failed)))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListMedicinesState.Loading
    )

    fun addMedicine(name: String, stock: Int, aisleId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            newMedicineUseCase(name, aisleId, stock)
                .onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: stringProvider.getString(R.string.error_medicine_create_failed))
                }
                .onSuccess { _operationState.value = OperationState.Success }
        }
    }

    fun filterByName(name: String) {
        _searchQuery.value = name
    }

    fun setSortOption(option: MedicineSortOption) {
        _sortOption.value = option
    }

    fun retry() {
        viewModelScope.launch {
            refreshSignal.tryEmit(Unit)
        }
    }
}

sealed interface ListMedicinesState {
    data object Loading : ListMedicinesState
    data class Success(val listMedicine: List<Medicine>) : ListMedicinesState
    data class Error(val error: String) : ListMedicinesState
}
