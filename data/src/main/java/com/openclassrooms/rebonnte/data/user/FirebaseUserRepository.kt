package com.openclassrooms.rebonnte.data.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.rebonnte.core.domain.model.User
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository @Inject constructor(private val firestore: FirebaseFirestore) :
    UserRepository {

    override suspend fun getUserById(userId: String): User? {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(User::class.java)
    }

    override suspend fun addUser(user: User): Result<Unit> = runCatching {
        Log.d("Debug LOg", "Repo adduser called with: $user")
        firestore.collection("users").document(user.id).set(user).await()
    }

}