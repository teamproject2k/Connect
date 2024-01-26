package com.example.connect.domain.useCase.fcm

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IFCMRepository
import javax.inject.Inject

class SendFCMUseCase @Inject constructor(private val repository: IFCMRepository) {

    suspend operator fun invoke(
        token: String,
        data: Map<String, String>,
        sendTo: String
    ): ResponseState<Nothing> {
        return repository.sendFCMMessage(token, data, sendTo)
    }
}