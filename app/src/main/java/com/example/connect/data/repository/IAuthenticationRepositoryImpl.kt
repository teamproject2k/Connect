package com.example.connect.data.repository

import com.example.connect.domain.repository.IAuthenticationRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class IAuthenticationRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) : IAuthenticationRepository {
    override suspend fun sendOtp(mobileNumber: String) {
    }
}