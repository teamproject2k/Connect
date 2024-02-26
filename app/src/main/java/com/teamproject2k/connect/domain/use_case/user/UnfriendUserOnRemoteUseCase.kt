package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnfriendUserOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Unfriends a user.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the current user.
     * @param requestedUserFirebaseId The Firebase ID of the user to unfriend.
     * @return A [ResponseState] containing either a success or failure message.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        requestedUserFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unFriendUserOnRemote(loggedInUserFirebaseId, requestedUserFirebaseId)
    }
}