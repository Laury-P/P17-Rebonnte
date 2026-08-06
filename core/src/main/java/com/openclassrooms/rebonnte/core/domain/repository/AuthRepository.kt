package com.openclassrooms.rebonnte.core.domain.repository

interface AuthRepository {
    fun getUserId() : String?
    suspend fun signOut() : Result<Unit>
    suspend fun deleteAccount()
    suspend fun signIn(email : String, password: String): Result<Unit>
    suspend fun signUp(email : String, password: String): Result<String>
}