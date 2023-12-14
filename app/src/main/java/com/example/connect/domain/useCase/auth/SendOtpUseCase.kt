package com.example.connect.domain.useCase.auth

import com.example.connect.common.ResponseState
import com.example.connect.domain.repository.IAuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the sendOtp method in the repository and updates the responseStateFlow.
     *
     * @param countryCode The country code of the mobile number.
     * @param mobileNumber The mobile number.
     * @param responseStateFlow The state flow that will be updated with the response.
     */
    suspend fun invoke(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    ) {
        repository.sendOtp(countryCode, mobileNumber, responseStateFlow)
    }
}