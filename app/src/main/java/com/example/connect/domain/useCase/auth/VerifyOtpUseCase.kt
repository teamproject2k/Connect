package com.example.connect.domain.useCase.auth

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(private val repository: IAuthenticationRepository) {
    /**
     * Invokes the verify OTP API.
     *
     * @param verificationId The verification ID.
     * @param otp The OTP.
     * @return A [ResponseState] containing the [FirebaseUser] if the OTP is valid, or an error otherwise.
     */
    suspend fun invoke(verificationId: String, otp: String): ResponseState<FirebaseUser> {
        return repository.verifyOtp(verificationId, otp)
    }
}