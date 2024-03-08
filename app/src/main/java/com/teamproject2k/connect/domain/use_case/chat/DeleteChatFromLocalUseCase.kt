package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteChatFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to delete a chat from the repository.
     *
     * @param chat The chat to be deleted.
     * @return The number of chats deleted.
     */
    suspend operator fun invoke(chat: ChatBean): Int {
        return repository.deleteChat(chat)
    }
}