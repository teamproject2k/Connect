package com.teamproject2k.connect.domain.useCase.auth

import com.google.firebase.auth.FirebaseUser
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(private val repository: IAuthenticationRepository) {
    /**
     * Invokes the verify OTP API.
     *
     * @param verificationId The verification ID.
     * @param otp The OTP.
     * @return A [ResponseState] containing the [FirebaseUser] if the OTP is valid, or an error otherwise.
     */
    suspend operator fun invoke(verificationId: String, otp: String): ResponseState<FirebaseUser> {
        return repository.verifyOtp(verificationId, otp)
    }
}