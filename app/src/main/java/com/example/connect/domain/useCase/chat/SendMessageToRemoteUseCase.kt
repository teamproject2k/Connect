package com.example.connect.domain.useCase.chat

import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IChatRepository
import javax.inject.Inject

class SendMessageToRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(message: ChatBean): ResponseState<Nothing> {
        return repository.sendChatMessageOnRemote(message)
    }
}