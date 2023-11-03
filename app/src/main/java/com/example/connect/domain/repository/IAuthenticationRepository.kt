package com.example.connect.domain.repository

import android.app.Activity
import android.content.Context
import com.example.connect.common.ResponseState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow

interface IAuthenticationRepository {
    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseState: MutableStateFlow<ResponseState<String>>
    )

    suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser?>

    suspend fun getUserDetails(userId: String)
}