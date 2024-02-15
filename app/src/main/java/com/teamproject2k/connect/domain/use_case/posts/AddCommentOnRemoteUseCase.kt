package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.CommentBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Adds a comment on the remote repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param comment The comment to be added.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success with a string representing the ID of the added comment if the addition was successful.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        comment: CommentBean
    ): ResponseState<String> {
        return repository.addCommentOnRemote(comment)
    }
}