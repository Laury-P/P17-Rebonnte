package com.openclassrooms.rebonnte.core.domain.model

sealed interface OperationState {
    object Idle : OperationState
    object Loading : OperationState
    object Success : OperationState
    data class Error(val error : String) : OperationState
}