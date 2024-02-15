package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class SendMessageToRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(message: ChatBean): ResponseState<Nothing> {
        return repository.sendChatMessageOnRemote(message)
    }
}