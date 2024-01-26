package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AcceptFriendRequestOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Accepts a friend request.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] containing the result of the operation.
     */
    suspend fun invoke(
        loggedInUserFirebaseId: String,
        requestUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.acceptFriendRequestOnRemote(loggedInUserFirebaseId, requestUserFirebaseId)
    }
}