package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostWithUserDetailsBean
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
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        savedPosts: ArrayList<String>
    ): ResponseState<List<PostWithUserDetailsBean>> {
        return repository.getSavedPostsWithUsersFromRemote(loggedInUserFirebaseId, savedPosts)
    }
}