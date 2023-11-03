package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.google.firebase.auth.FirebaseUser

interface IAuthenticationRepository {
    suspend fun sendOtp(mobileNumber: String)

    suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser?>

    suspend fun getUserDetails(userId: String)
}