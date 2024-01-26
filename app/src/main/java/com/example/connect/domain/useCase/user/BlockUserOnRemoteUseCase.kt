package com.example.connect.domain.useCase.user

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class BlockUserOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Blocks a user.
     *
     * @param loggedInUserFirebaseId The ID of the current user.
     * @param requestedUserFirebaseId The ID of the user to block.
     * @return A [ResponseState] containing the result of the operation.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.blockUserOnRemote(loggedInUserFirebaseId, requestedUserFirebaseId)
    }
}