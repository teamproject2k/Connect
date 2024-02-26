package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeletePostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Deletes a post from the local repository using the specified post Firebase ID.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param postFirebaseId The Firebase ID of the post to be deleted from the local repository.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(postFirebaseId: String) {
        repository.deletePostFromLocal(postFirebaseId)
    }
}