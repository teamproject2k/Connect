package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddPostToDbUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend fun invoke(postDetailList: PostBean): Long {
        return repository.addPostToDb(postDetailList)
    }
}