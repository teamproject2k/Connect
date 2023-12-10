package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class AddPostListToDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Adds a post to the local database.
     *
     * @param postDetails The details of the post to add.
     * @return The ID of the newly added post.
     */
    suspend fun addPostToDb(postDetails: PostDetails): Long {
        return repository.addPostToLocal(postDetails)
    }

}