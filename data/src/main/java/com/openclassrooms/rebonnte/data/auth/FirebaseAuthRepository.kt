package com.openclassrooms.rebonnte.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth) : AuthRepository
{
    override fun getUserId(): String? = auth.currentUser?.uid

    override suspend fun signOut(): Result<Unit> = runCatching{
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: throw Exception("Impossible de récupérer l'UID")
    }
}