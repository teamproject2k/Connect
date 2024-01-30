package com.example.connect.domain.useCase.chat

import com.example.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetChatListFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(loggedInUserFirebaseId: String) {
        return repository.getChatListFromRemote(loggedInUserFirebaseId)
    }
}