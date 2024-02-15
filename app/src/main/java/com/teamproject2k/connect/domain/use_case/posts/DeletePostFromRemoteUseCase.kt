package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeletePostFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Deletes a post from the remote repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param postFirebaseId The Firebase ID of the post to be deleted.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the post was successfully deleted.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(postFirebaseId: String): ResponseState<Nothing> {
        return repository.deletePostFromRemote(postFirebaseId)
    }
}