package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(private val repository: IAuthenticationRepository) {
    suspend fun invoke(verificationId: String, otp: String): ResponseState<FirebaseUser> {
        return repository.verifyOtp(verificationId, otp)
    }
}