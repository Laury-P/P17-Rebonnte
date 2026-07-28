package com.openclassrooms.rebonnte.data.di

import com.firebase.ui.auth.FirebaseAuthUI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideAuthUi() : FirebaseAuthUI {
        return FirebaseAuthUI.getInstance()
    }

}