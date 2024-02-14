package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class UpdateMessageOnLocalUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(message: ChatBean): Int {
        return repository.updateChatOnLocal(message)
    }
}