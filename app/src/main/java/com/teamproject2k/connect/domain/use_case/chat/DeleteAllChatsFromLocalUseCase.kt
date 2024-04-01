package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteAllChatsFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to delete all chats from the repository.
     *
     * @return The number of chats deleted.
     */
    suspend operator fun invoke(): Int {
        return repository.deleteAllChats()
    }
}