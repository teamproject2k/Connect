package com.example.connect.domain.useCase.chat

import com.example.connect.domain.models.UserWithChatListBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetChatListFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>> {
        return repository.getChatListFromRemote(loggedInUserFirebaseId)
    }
}