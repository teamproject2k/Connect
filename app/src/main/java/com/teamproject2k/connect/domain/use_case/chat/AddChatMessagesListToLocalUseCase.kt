package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class AddChatMessagesListToLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Adds a list of chat messages to the local repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param chatMessagesList The list of chat messages to be added to the local repository.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(chatMessagesList: List<ChatBean>) {
        repository.addChatListToLocal(chatMessagesList)
    }
}