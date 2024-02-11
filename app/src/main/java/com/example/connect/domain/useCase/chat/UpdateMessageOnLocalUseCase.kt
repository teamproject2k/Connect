package com.example.connect.domain.useCase.chat

import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.repository.IChatRepository
import javax.inject.Inject

class UpdateMessageOnLocalUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(message: ChatBean): Int {
        return repository.updateChatOnLocal(message)
    }
}