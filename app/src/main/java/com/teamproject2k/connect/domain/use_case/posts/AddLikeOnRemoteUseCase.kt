package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddLikeOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Adds a like on a post on the remote repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param postFirebaseId The Firebase ID of the post on which the like should be added.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the like was successfully added.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.addLikeOnPostOnRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}