package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UpdatePostDetailsOnLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the function to update post details in the local database.
     *
     * @param postDetails The details of the post to be updated.
     * @return The number of posts updated in the local database.
     */
    suspend operator fun invoke(postDetails: PostBean): Int {
        return repository.updatePostDetailsOnLocal(postDetails)
    }
}