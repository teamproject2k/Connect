package com.example.connect.domain.repository

import com.example.connect.common.ResponseState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow

interface IAuthenticationRepository {
    /**
     * Sends an OTP to the given mobile number.
     *
     * @param countryCode The country code of the mobile number.
     * @param mobileNumber The mobile number.
     * @param responseStateFlow The state flow that will be updated with the response.
     */
    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    )

    /**
     * Verifies the OTP and returns the FirebaseUser if successful.
     *
     * @param verificationId The verification ID.
     * @param otp The OTP.
     * @return The response state.
     */
    suspend fun verifyOtp(verificationId: String, otp: String): ResponseState<FirebaseUser>

}