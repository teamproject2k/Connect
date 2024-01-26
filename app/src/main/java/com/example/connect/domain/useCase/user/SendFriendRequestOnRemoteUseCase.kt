package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class SendFriendRequestOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Sends a friend request to the specified user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to send the friend request to.
     * @return A [ResponseState] containing the result of the operation.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.sendFriendRequestOnRemote(loggedInUserFirebaseId, requestedUserFirebaseId)
    }
}