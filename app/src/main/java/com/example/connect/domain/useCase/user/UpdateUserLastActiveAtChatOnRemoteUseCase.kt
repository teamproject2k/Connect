package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserLastActiveAtChatOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(
        lastActiveAtChat: Long,
        loggedInUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.updateUserLastActiveAtChatOnRemote(lastActiveAtChat,loggedInUserFirebaseId)
    }
}