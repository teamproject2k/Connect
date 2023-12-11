package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class AddPostListToDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Invokes the repository to add a list of posts to local storage.
     *
     * @param postDetails The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend fun invoke(postDetails: List<PostBean>): LongArray {
        return repository.addPostListToDb(postDetails)
    }

}