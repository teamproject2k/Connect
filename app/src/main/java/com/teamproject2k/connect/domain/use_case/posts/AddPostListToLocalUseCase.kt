package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddPostListToLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the repository to add a list of posts to local storage.
     *
     * @param postDetails The list of posts to add.
     * @return The IDs of the posts that were added.
     */
    suspend operator fun invoke(postDetails: List<PostBean>): LongArray {
        return repository.addPostListToLocal(postDetails)
    }
}