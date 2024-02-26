package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class RemoveLikeForCommentFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Removes a like for a comment from the remote repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param commentId The ID of the comment for which the like should be removed.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the like for the comment was successfully removed.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(commentId: String, loggedInUserFirebaseId: String): ResponseState<Nothing> {
        return repository.removeLikeForCommentFromRemote(commentId, loggedInUserFirebaseId)
    }
}