package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddPostListToLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the repository to add a list of posts to local storage.
     *
     * @param postDetails The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend fun invoke(postDetails: List<PostBean>): LongArray {
        return repository.addPostListToLocal(postDetails)
    }
}