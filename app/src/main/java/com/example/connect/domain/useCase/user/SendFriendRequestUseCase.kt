package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class SendFriendRequestUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Sends a friend request to the specified user.
     *
     * @param currentUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to send the friend request to.
     * @return A [ResponseState] containing the result of the operation.
     */
    suspend fun invoke(
        currentUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.sendFriendRequest(currentUserFirebaseId, requestedUserFirebaseId)
    }
}