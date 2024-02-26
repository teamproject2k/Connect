package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostWithUserDetailsBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsWithUsersFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Retrieves post details with user information from the local repository.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param loggedInUserBlockedList The list of Firebase IDs of users blocked by the logged-in user.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success with a list of post details with user information if retrieval was successful.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        loggedInUserBlockedList: List<String>
    ): ResponseState<List<PostWithUserDetailsBean>> {
        return repository.getPostDetailsWithUsersFromLocal(
            loggedInUserFirebaseId,
            loggedInUserBlockedList
        )
    }
}