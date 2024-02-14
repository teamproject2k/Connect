package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class UpdateLastSeenAtOnLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(
        chatId: String,
        lastSeenAt: Long,
    ): Int {
        return repository.updateLastSeenAtOnLocal(chatId, lastSeenAt)
    }
}