package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteMessageOnRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to delete a message on the remote server.
     *
     * @param deletedBy The Firebase ID of the user who initiated the message deletion.
     * @param senderId The Firebase ID of the message sender.
     * @param receiverId The Firebase ID of the message receiver.
     * @param messageId The ID of the message to be deleted.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend operator fun invoke(
        deletedBy: String,
        senderId: String,
        receiverId: String,
        messageId: String
    ): ResponseState<Nothing> {
        return repository.deleteMessageOnRemote(deletedBy, senderId, receiverId, messageId)
    }
}