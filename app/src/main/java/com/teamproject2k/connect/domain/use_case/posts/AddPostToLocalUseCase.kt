package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddPostToLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend operator fun invoke(postDetailList: PostBean): Long {
        return repository.addPostToLocal(postDetailList)
    }
}