package com.openclassrooms.rebonnte.feature.auth

import app.cash.turbine.test
import com.openclassrooms.rebonnte.core.domain.model.OperationState
import com.openclassrooms.rebonnte.core.domain.model.User
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class LogViewModelTest {

    private val authRepository = mockk<AuthRepository>()
    private val userRepository = mockk<UserRepository>()
    private val viewModel = LogViewModel(authRepository, userRepository)

    @Test
    fun `signIn should emit Success when authRepository returns success`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        coEvery { authRepository.signIn(email, password) } returns Result.success(Unit)

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.signIn(email, password)

            // Then
            assertEquals(OperationState.Loading, awaitItem())
            assertEquals(OperationState.Success, awaitItem())
        }
        coVerify { authRepository.signIn(email, password) }
    }

    @Test
    fun `signIn should emit Error when authRepository returns failure`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        val errorMsg = "Login failed"
        coEvery { authRepository.signIn(email, password) } returns Result.failure(Exception(errorMsg))

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.signIn(email, password)

            // Then
            assertEquals(OperationState.Loading, awaitItem())
            val state = awaitItem() as OperationState.Error
            assertEquals(errorMsg, state.error)
        }
    }

    @Test
    fun `signUp should emit Success when auth and user addition are successful`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        val name = "Test User"
        val uid = "user123"
        val newUser = User(id = uid, email = email, name = name)

        coEvery { authRepository.signUp(email, password) } returns Result.success(uid)
        coEvery { userRepository.addUser(newUser) } returns Result.success(Unit)

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.signUp(email, password, name)

            // Then
            assertEquals(OperationState.Loading, awaitItem())
            assertEquals(OperationState.Success, awaitItem())
        }
        coVerify { authRepository.signUp(email, password) }
        coVerify { userRepository.addUser(newUser) }
    }

    @Test
    fun `signUp should emit Error when authRepository fails`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        val name = "Test User"
        val errorMsg = "Auth failed"
        coEvery { authRepository.signUp(email, password) } returns Result.failure(Exception(errorMsg))

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.signUp(email, password, name)

            // Then
            assertEquals(OperationState.Loading, awaitItem())
            val state = awaitItem() as OperationState.Error
            assertEquals(errorMsg, state.error)
        }
    }

    @Test
    fun `signUp should emit Error when userRepository fails to add user`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        val name = "Test User"
        val uid = "user123"
        val errorMsg = "Database error"
        val newUser = User(id = uid, email = email, name = name)

        coEvery { authRepository.signUp(email, password) } returns Result.success(uid)
        coEvery { userRepository.addUser(newUser) } returns Result.failure(Exception(errorMsg))

        viewModel.operationState.test {
            assertEquals(OperationState.Idle, awaitItem())

            // When
            viewModel.signUp(email, password, name)

            // Then
            assertEquals(OperationState.Loading, awaitItem())
            val state = awaitItem() as OperationState.Error
            assertEquals(errorMsg, state.error)
        }
    }
}