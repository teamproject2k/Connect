package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnBlockUserOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Unblocks a user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to unblock.
     * @return A [ResponseState] containing either [Nothing] if the user was successfully unblocked or an error message if the unblocking failed.
     */
    suspend fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unBlockUserOnRemote(loggedInUserFirebaseId, requestedUserFirebaseId)
    }
}