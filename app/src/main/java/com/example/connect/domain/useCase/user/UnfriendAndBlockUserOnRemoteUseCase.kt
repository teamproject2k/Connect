package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnfriendAndBlockUserOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Unfriends and blocks a user.
     *
     * @param loggedInUserFirebaseId The ID of the current user.
     * @param requestedUserFirebaseId The ID of the user to unfriend and block.
     * @return A response state indicating the success or failure of the operation.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unFriendAndBlockUserOnRemote(
            loggedInUserFirebaseId,
            requestedUserFirebaseId
        )
    }
}