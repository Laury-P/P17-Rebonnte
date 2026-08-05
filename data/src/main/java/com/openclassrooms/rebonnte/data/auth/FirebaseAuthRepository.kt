package com.openclassrooms.rebonnte.data.auth

import android.content.Context
import android.util.Log
import com.firebase.ui.auth.FirebaseAuthUI
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authUI: FirebaseAuthUI,
    private val auth: FirebaseAuth) : AuthRepository
{
    override fun getUserId(): String? = authUI.auth.currentUser?.uid

    override suspend fun signOut(): Result<Unit> = runCatching{
        authUI.signOut(context)
    }

    override suspend fun deleteAccount() {
        Log.d("DEBUG LOG", "Delete account called")
        authUI.delete(context)
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: throw Exception("Impossible de récupérer l'UID")
    }
}