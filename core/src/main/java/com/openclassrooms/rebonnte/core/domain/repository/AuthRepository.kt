package com.openclassrooms.rebonnte.core.domain.repository

interface AuthRepository {
    fun getUserId() : String?
    suspend fun signOut() : Result<Unit>
}