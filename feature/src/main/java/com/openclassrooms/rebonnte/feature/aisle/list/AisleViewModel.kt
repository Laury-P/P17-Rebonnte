package com.openclassrooms.rebonnte.feature.aisle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.Aisle
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.util.StringProvider
import com.openclassrooms.rebonnte.feature.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AisleViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val stringProvider: StringProvider
) :
    ViewModel() {

    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState = _operationState.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    var uiState: StateFlow<ListAislesState> = refreshSignal
        .flatMapLatest{
            medicineRepository.getListAisles()
                .map { list ->
                    ListAislesState.Success(list) as ListAislesState
                }
                .catch { error ->
                    emit(ListAislesState.Error(error.message ?: stringProvider.getString(R.string.error_unknown)))
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListAislesState.Loading
        )


    fun addAisle(title: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val newAisle = Aisle(
                name = title,
                aisleId = medicineRepository.generateAisleId()
            )
            medicineRepository.addAisle(newAisle)
                .onFailure { error ->
                    _operationState.value = OperationState.Error(
                        error.message ?: stringProvider.getString(R.string.error_aisle_add_failed)
                    )
                }
                .onSuccess {
                    _operationState.value = OperationState.Success
                }
        }
    }

    fun retry() {
        viewModelScope.launch { refreshSignal.emit(Unit) }
    }
}

sealed interface ListAislesState {
    data object Loading : ListAislesState
    data class Success(val listAisle: List<Aisle>) : ListAislesState
    data class Error(val error: String) : ListAislesState
}