package com.example.connect.di

import com.example.connect.data.repository.IAuthenticationRepositoryImpl
import com.example.connect.domain.repository.IAuthenticationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun getFirebaseAuth(): FirebaseAuth {
        val firebaseAuth = Firebase.auth
        firebaseAuth.useAppLanguage()
        return firebaseAuth
    }


    @Provides
    @Singleton
    fun getAuthRepository(firebaseAuth: FirebaseAuth): IAuthenticationRepository = IAuthenticationRepositoryImpl(firebaseAuth)
}