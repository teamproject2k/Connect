package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class AddPostToDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Adds a list of posts to the local database.
     *
     * @param postDetailList The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend fun addPostListToDb(postDetailList: List<PostDetails>): LongArray {
        return repository.addPostListToLocal(postDetailList)
    }
}