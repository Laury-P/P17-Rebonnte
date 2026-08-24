package com.openclassrooms.rebonnte.feature.fakes

import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var currentUserId: String? = null
    var shouldReturnError = false

    override fun getUserId(): String? = currentUserId

    override suspend fun signOut(): Result<Unit> {
        currentUserId = null
        return Result.success(Unit)
    }

    override suspend fun deleteAccount() {
        currentUserId = null
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Auth Error"))
        currentUserId = "fake_user_id"
        return Result.success(Unit)
    }

    override suspend fun signUp(email: String, password: String): Result<String> {
        if (shouldReturnError) return Result.failure(Exception("Auth Error"))
        currentUserId = "fake_user_id"
        return Result.success(currentUserId!!)
    }
}
