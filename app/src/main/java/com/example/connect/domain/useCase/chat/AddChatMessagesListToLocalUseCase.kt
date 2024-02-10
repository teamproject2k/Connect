package com.example.connect.domain.useCase.chat

import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.repository.IChatRepository
import javax.inject.Inject

class AddChatMessagesListToLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(chatMessagesList: List<ChatBean>) {
        repository.addChatListToLocal(chatMessagesList)
    }
}