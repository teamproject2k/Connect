package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the database.
     *
     * @param userFirebaseId The fire base id of the post.
     * @return The post details.
     */
    suspend fun invoke(userFirebaseId: String): List<PostBean> {
        return repository.getPostDetailsFromLocal(userFirebaseId)
    }
}