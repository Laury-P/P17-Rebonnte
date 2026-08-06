package com.openclassrooms.rebonnte.feature.auth

import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.feature.utils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class AuthViewModelTest {

    private val authRepository = mockk<AuthRepository>()
    private val viewModel = AuthViewModel(authRepository)

    @Test
    fun `signOut should call authRepository signOut`() = runTest {
        // Given
        coEvery { authRepository.signOut() } returns Result.success(Unit)

        // When
        viewModel.signOut()
        advanceUntilIdle()

        // Then
        coVerify { authRepository.signOut() }
    }
}
