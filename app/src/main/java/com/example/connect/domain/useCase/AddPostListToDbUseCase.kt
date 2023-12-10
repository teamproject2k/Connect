package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class AddPostListToDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Invokes the repository to add a list of posts to local storage.
     *
     * @param postDetails The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend fun invoke(postDetails: List<PostDetails>): LongArray {
        return repository.addPostListToLocal(postDetails)
    }

}