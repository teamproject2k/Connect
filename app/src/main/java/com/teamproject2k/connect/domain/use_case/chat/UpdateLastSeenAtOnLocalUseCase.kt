package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class UpdateLastSeenAtOnLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to update the last seen timestamp for a chat in the local database.
     *
     * @param chatId The ID of the chat for which the last seen timestamp is being updated.
     * @param lastSeenAt The new last seen timestamp.
     * @return The number of chats updated in the local database.
     */
    suspend operator fun invoke(
        chatId: String,
        lastSeenAt: Long,
    ): Int {
        return repository.updateLastSeenAtOnLocal(chatId, lastSeenAt)
    }
}