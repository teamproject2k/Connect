package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UpdatePostVisibilityOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Updates the visibility of a post on the remote server using the specified post Firebase ID and scope name.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param postFirebaseId The Firebase ID of the post to be updated.
     * @param postScopeName The new scope name for the post visibility.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the visibility update was successful.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(postFirebaseId: String, postScopeName: String): ResponseState<Nothing> {
        return repository.updatePostVisibilityOnRemote(postFirebaseId, postScopeName)
    }
}