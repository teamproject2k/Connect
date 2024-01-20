package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnfriendUserUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Unfriends a user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to unfriend.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unFriendUserOnRemote(loggedInUserFirebaseId, requestedUserFirebaseId)
    }
}