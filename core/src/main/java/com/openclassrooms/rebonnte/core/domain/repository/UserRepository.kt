package com.openclassrooms.rebonnte.core.domain.repository

import com.openclassrooms.rebonnte.core.domain.model.User

interface UserRepository {
    suspend fun getUserById (userId : String) : User?
    suspend fun addUser(user: User) : Result<Unit>
}