package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class RemoveFriendRequestOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Removes a friend request from the database.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user who sent the friend request.
     * @return A [ResponseState] object that indicates the success or failure of the operation.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.removeFriendRequestOnRemote(
            loggedInUserFirebaseId,
            requestedUserFirebaseId
        )
    }
}