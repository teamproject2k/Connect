package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostOfUserFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the function to delete all posts of a user from the local database.
     *
     * @param userFirebaseId The Firebase ID of the user whose posts are to be deleted.
     * @return The number of posts deleted from the local database.
     */
    suspend operator fun invoke(userFirebaseId: String): Int {
        return repository.deleteAllPostOfUserFromLocal(userFirebaseId)
    }
}