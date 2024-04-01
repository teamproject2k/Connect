package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteOnlyFriendsOnlyPostOfUserFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the function to delete all posts of a user with friends-only visibility from the local database.
     *
     * @param userFireBaseId The Firebase ID of the user whose posts with friends-only visibility are to be deleted.
     * @return The number of posts deleted from the local database.
     */
    suspend operator fun invoke(userFireBaseId: String): Int {
        return repository.deleteAllPostOfUserWithFriendsOnlyVisibilityFromLocal(userFireBaseId)
    }
}