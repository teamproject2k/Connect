package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Deletes a comment from the remote server using the specified comment ID, post Firebase ID, and delete count.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param commentId The ID of the comment to be deleted.
     * @param postFirebaseId The Firebase ID of the post from which the comment should be deleted.
     * @param deleteCount The number of times the comment should be deleted.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success with a message if the comment was successfully deleted.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        commentId: String,
        postFirebaseId: String,
        deleteCount: Int,
    ): ResponseState<String> {
        return repository.deleteCommentOnRemote(commentId, postFirebaseId, deleteCount)
    }
}