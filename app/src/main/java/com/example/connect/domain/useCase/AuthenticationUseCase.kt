package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class AuthenticationUseCase @Inject constructor(private val repository: IAuthenticationRepository) {
    suspend fun sendOtp(
        countryCode: String,
        mobileNumber: String,
        responseStateFlow: MutableStateFlow<ResponseState<Pair<String, String>>>
    ) = repository.sendOtp(countryCode, mobileNumber, responseStateFlow)


    suspend fun getUserDetails(userId: String) = repository.getUserDetails(userId)

    suspend fun getUsersFromName(name: String) = repository.getUsersFromName(name)



}