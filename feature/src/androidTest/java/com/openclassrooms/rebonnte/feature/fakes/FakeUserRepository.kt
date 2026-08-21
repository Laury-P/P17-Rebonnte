package com.openclassrooms.rebonnte.feature.fakes

import com.openclassrooms.rebonnte.core.domain.model.User
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository

class FakeUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()
    var shouldReturnError = false

    override suspend fun getUserById(userId: String): User? {
        return users[userId]
    }

    override suspend fun addUser(user: User): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Fake Error"))
        users[user.id] = user
        return Result.success(Unit)
    }
}
