package com.openclassrooms.rebonnte.feature.di

import com.openclassrooms.rebonnte.core.domain.repository.AuthRepository
import com.openclassrooms.rebonnte.core.domain.repository.MedicineRepository
import com.openclassrooms.rebonnte.core.domain.repository.UserRepository
import com.openclassrooms.rebonnte.data.di.RepositoryModule
import com.openclassrooms.rebonnte.feature.fakes.FakeAuthRepository
import com.openclassrooms.rebonnte.feature.fakes.FakeMedicineRepository
import com.openclassrooms.rebonnte.feature.fakes.FakeUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideFakeAuthRepository(): FakeAuthRepository = FakeAuthRepository()

    @Provides
    @Singleton
    fun provideAuthRepository(fake: FakeAuthRepository): AuthRepository = fake

    @Provides
    @Singleton
    fun provideFakeMedicineRepository(): FakeMedicineRepository = FakeMedicineRepository()

    @Provides
    @Singleton
    fun provideMedicineRepository(fake: FakeMedicineRepository): MedicineRepository = fake

    @Provides
    @Singleton
    fun provideFakeUserRepository(): FakeUserRepository = FakeUserRepository()

    @Provides
    @Singleton
    fun provideUserRepository(fake: FakeUserRepository): UserRepository = fake
}
