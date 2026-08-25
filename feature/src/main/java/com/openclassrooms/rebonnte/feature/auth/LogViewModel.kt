package com.openclassrooms.rebonnte.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.domain.model.User
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import com.openclassrooms.rebonnte.core.util.StringProvider
import com.openclassrooms.rebonnte.feature.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LogViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState = _operationState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            authRepository.signIn(email, password)
                .onSuccess {
                    _operationState.value = OperationState.Success
                }
                .onFailure {
                    _operationState.value =
                        OperationState.Error(it.message ?: stringProvider.getString(R.string.error_login))
                }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val authResult = authRepository.signUp(email, password)
            authResult.onSuccess { uid ->
                val newUser = User(id = uid, email = email, name = name)
                userRepository.addUser(newUser)
                    .onSuccess {
                        _operationState.value = OperationState.Success
                    }
                    .onFailure {
                        _operationState.value = OperationState.Error(
                            it.message ?: stringProvider.getString(R.string.error_user_save)
                        )
                    }
            }.onFailure {
                _operationState.value =
                    OperationState.Error(it.message ?: stringProvider.getString(R.string.error_signup))
            }
        }
    }
}
