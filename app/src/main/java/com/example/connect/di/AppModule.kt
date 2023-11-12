package com.example.connect.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.repository.IAuthenticationRepositoryImpl
import com.example.connect.data.repository.IHomeRepositoryImpl
import com.example.connect.domain.repository.IAuthenticationRepository
import com.example.connect.domain.repository.IHomeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun getSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("com.example.connect_shared_pref", MODE_PRIVATE)
    }


    @Provides
    @Singleton
    fun getFireStore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun getAuthRepository(
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IAuthenticationRepository =
        IAuthenticationRepositoryImpl(firebaseAuth, fireStore, appDatabase)

    @Provides
    @Singleton
    fun getRoomDatabase(@ApplicationContext context: Context): AppDatabase {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, "com.example.connect.app_database")
        return database.build()
    }


    @Provides
    @Singleton
    fun getHomeRepository(
        appDatabase: AppDatabase,
        fireStore: FirebaseFirestore
    ): IHomeRepository =
        IHomeRepositoryImpl(appDatabase, fireStore)
}