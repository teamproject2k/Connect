package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class WithdrawFriendRequestOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the withdrawFriendRequest method in the repository.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the requested user.
     * @return A ResponseState object containing the result of the operation.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.withdrawFriendRequestOnRemote(
            loggedInUserFirebaseId,
            requestedUserFirebaseId
        )
    }
}