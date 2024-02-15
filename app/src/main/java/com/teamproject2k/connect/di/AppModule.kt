package com.teamproject2k.connect.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.teamproject2k.connect.BuildConfig
import com.teamproject2k.connect.data.local_db.AppDatabase
import com.teamproject2k.connect.data.remote.IRemoteRepository
import com.teamproject2k.connect.data.repository.IAppLocalRepositoryImpl
import com.teamproject2k.connect.data.repository.IAuthenticationRepositoryImpl
import com.teamproject2k.connect.data.repository.IChatRepositoryImpl
import com.teamproject2k.connect.data.repository.IDeviceIdRepositoryImpl
import com.teamproject2k.connect.data.repository.IFCMRepositoryImpl
import com.teamproject2k.connect.data.repository.IPostRepositoryImpl
import com.teamproject2k.connect.data.repository.IStoryRepositoryImpl
import com.teamproject2k.connect.data.repository.IUploadRepositoryImpl
import com.teamproject2k.connect.data.repository.IUserRepositoryImpl
import com.teamproject2k.connect.domain.repository.IAppLocalRepository
import com.teamproject2k.connect.domain.repository.IAuthenticationRepository
import com.teamproject2k.connect.domain.repository.IChatRepository
import com.teamproject2k.connect.domain.repository.IDeviceIdRepository
import com.teamproject2k.connect.domain.repository.IFCMRepository
import com.teamproject2k.connect.domain.repository.IPostRepository
import com.teamproject2k.connect.domain.repository.IStoryRepository
import com.teamproject2k.connect.domain.repository.IUploadFileRepository
import com.teamproject2k.connect.domain.repository.IUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        return context.getSharedPreferences("${context.packageName}.shared_pref", MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun getFireStore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun getFirebaseDatabase(): FirebaseDatabase = Firebase.database

    @Provides
    @Singleton
    fun getFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun getFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun getRoomDatabase(@ApplicationContext context: Context): AppDatabase {
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "${context.packageName}.app_database"
        )
        return database.build()
    }

    @Provides
    @Singleton
    fun getAuthRepository(
        firebaseAuth: FirebaseAuth
    ): IAuthenticationRepository =
        IAuthenticationRepositoryImpl(firebaseAuth)

    @Provides
    @Singleton
    fun getUserRepository(
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IUserRepository = IUserRepositoryImpl(fireStore, appDatabase)

    @Provides
    @Singleton
    fun getPostRepository(
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IPostRepository = IPostRepositoryImpl(fireStore, appDatabase)

    @Provides
    @Singleton
    fun getDeviceIdRepository(
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IDeviceIdRepository = IDeviceIdRepositoryImpl(fireStore, appDatabase)

    @Provides
    @Singleton
    fun getIUploadFileRepository(
        firebaseStorage: FirebaseStorage
    ): IUploadFileRepository =
        IUploadRepositoryImpl(firebaseStorage)

    @Provides
    @Singleton
    fun getIStoryRepository(
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IStoryRepository =
        IStoryRepositoryImpl(fireStore, appDatabase)

    @Provides
    @Singleton
    fun getIChatRepository(
        firebaseDatabase: FirebaseDatabase,
        fireStore: FirebaseFirestore,
        appDatabase: AppDatabase
    ): IChatRepository =
        IChatRepositoryImpl(firebaseDatabase, fireStore, appDatabase)

    @Provides
    @Singleton
    fun getIAppLocalRepository(
        appDatabase: AppDatabase
    ): IAppLocalRepository =
        IAppLocalRepositoryImpl(appDatabase)

    @Provides
    @Singleton
    fun getOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return if (BuildConfig.DEBUG) {
            val chuckerInterceptor = ChuckerInterceptor.Builder(context)
                .collector(ChuckerCollector(context))
                .alwaysReadResponseBody(true)
                .build()
            OkHttpClient.Builder()
                .addInterceptor(chuckerInterceptor)
                .build()
        } else {
            OkHttpClient.Builder()
                .build()
        }
    }

    @Provides
    @Singleton
    fun getRetrofitInstance(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/v1/projects/connect-d6237/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun getIRemoteRepository(
        retrofit: Retrofit
    ): IRemoteRepository = retrofit
        .create(IRemoteRepository::class.java)

    @Provides
    @Singleton
    fun getIFCMRepository(
        firebaseMessaging: FirebaseMessaging,
        remoteRepository: IRemoteRepository
    ): IFCMRepository =
        IFCMRepositoryImpl(firebaseMessaging, remoteRepository)

}