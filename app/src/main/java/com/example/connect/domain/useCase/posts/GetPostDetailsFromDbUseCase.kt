package com.example.connect.domain.useCase.posts

import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetPostDetailsFromDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Gets the post details from the database.
     *
     * @param fireBaseId The fire base id of the post.
     * @return The post details.
     */
    suspend fun invoke(fireBaseId: String): List<PostDetails>? {
        return repository.getPostDetailsFromLocal(fireBaseId)
    }

}