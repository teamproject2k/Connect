package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteMessageOnRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(
        deletedBy: String,
        senderId: String,
        receiverId: String,
        messageId: String
    ): ResponseState<Nothing> {
        return repository.deleteMessageOnRemote(deletedBy, senderId, receiverId, messageId)
    }
}