package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class SendMessageToRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to send a chat message to the remote server.
     *
     * @param message The chat message to be sent.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend operator fun invoke(message: ChatBean): ResponseState<Nothing> {
        return repository.sendChatMessageOnRemote(message)
    }
}