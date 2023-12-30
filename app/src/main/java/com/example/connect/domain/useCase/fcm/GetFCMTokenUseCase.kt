package com.example.connect.domain.useCase.fcm

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IFCMRepository
import javax.inject.Inject

class GetFCMTokenUseCase @Inject constructor(private val repository: IFCMRepository) {

    suspend fun invoke(): ResponseState<String> {
        return repository.getFCMToken()
    }
}