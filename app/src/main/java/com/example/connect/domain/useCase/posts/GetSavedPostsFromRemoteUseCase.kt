package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetSavedPostsFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the database.
     *
     * @param fireBaseId The fire base id of the post.
     * @return The post details.
     */
    suspend fun invoke(savedPosts: ArrayList<String>): ResponseState<List<PostBean>> {
        return repository.getSavedPostsFromRemote(savedPosts)
    }
}