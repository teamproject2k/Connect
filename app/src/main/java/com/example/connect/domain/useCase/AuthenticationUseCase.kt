package com.example.connect.domain.useCase

import android.app.Activity
import android.content.Context
import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class AuthenticationUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseState: MutableStateFlow<ResponseState<String>>
    ) = repository.sendOtp(countryCode, mobileNumber, responseState)

    suspend fun verifyOtp(verificationId: String, otp: String) =
        repository.verifyOtp(verificationId, otp)

    suspend fun getUserDetails(userId: String) = repository.getUserDetails(userId)
}