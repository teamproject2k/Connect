package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteChatFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(chat: ChatBean): Int {
        return repository.deleteChat(chat)
    }
}