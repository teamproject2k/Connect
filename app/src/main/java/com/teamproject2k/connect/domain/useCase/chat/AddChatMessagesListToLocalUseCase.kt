package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class AddChatMessagesListToLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(chatMessagesList: List<ChatBean>) {
        repository.addChatListToLocal(chatMessagesList)
    }
}