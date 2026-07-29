package com.openclassrooms.rebonnte.data.di

import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import com.openclassrooms.rebonnte.data.auth.FirebaseAuthRepository
import com.openclassrooms.rebonnte.data.medecine.FirebaseMedicineRepository
import com.openclassrooms.rebonnte.data.user.FirebaseUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ) : AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        firebaseUserRepository: FirebaseUserRepository
    ) : UserRepository

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        firebaseMedicineRepository: FirebaseMedicineRepository
    ) : MedicineRepository
}