package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the function to delete all posts from the local database.
     *
     * @return The number of posts deleted from the local database.
     */
    suspend operator fun invoke(): Int {
        return repository.deleteAllPostFomLocal()
    }
}