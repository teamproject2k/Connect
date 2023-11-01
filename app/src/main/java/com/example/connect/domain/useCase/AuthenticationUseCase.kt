package com.example.connect.domain.useCase

import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class AuthenticationUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    suspend fun sendOtp(mobileNumber: String) = repository.sendOtp(mobileNumber)
}