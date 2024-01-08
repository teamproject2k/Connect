package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetSavedPostsWithUsersFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the database.
     *
     * @param firebaseId The fire base id of the post.
     * @return The post details.
     */
    suspend fun invoke(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<Pair<ArrayList<PostBean>, ArrayList<UsersBean>>> {
        return repository.getSavedPostsWithUsersFromRemote(loggedInUserFirebaseId, savedPosts)
    }
}