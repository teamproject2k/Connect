package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnfriendUserUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend fun invoke(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unFriendUser(currentUserFirebaseId, requestedUserFirebaseId)
    }
}