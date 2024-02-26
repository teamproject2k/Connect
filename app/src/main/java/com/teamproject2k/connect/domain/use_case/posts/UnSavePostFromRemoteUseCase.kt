package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnSavePostFromRemoteUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Unsaves a post from the remote server using the specified user Firebase ID and post Firebase ID.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param postFirebaseId The Firebase ID of the post to be unsaved.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the post was successfully unsaved.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.unSavePostFromRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}