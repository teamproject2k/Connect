package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class DeleteAllChatsFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(): Int {
        return repository.deleteAllChats()
    }
}