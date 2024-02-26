package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.CommentWithUserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetAllCommentsWithUsersFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Retrieves all comments with user information for a post from the remote repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param postFirebaseId The Firebase ID of the post for which comments are to be retrieved.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success with a map of comments with user information if retrieval was successful.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        postFirebaseId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<MutableMap<CommentWithUserBean, ArrayList<CommentWithUserBean>>> {
        return repository.getAllCommentsWithUsersFromRemote(postFirebaseId, loggedInUserFirebaseId)
    }
}