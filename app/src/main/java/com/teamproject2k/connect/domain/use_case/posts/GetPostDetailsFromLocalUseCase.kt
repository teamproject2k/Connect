package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class GetPostDetailsFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Gets the post details from the database.
     *
     * @param userFirebaseId The fire base id of the post.
     * @return The post details.
     */
    suspend operator fun invoke(userFirebaseId: String): List<PostBean> {
        return repository.getPostDetailsFromLocal(userFirebaseId)
    }
}