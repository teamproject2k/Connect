package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserStatusOnDbUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to update the other users' status on the database.
     *
     * @param loggedInUserFirebaseId The current user's Firebase ID.
     * @param otherUsersStatus A mutable map of other users' Firebase IDs to their status.
     * @return The number of users whose status was successfully updated.
     */
    suspend fun invoke(
        loggedInUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int {
        return repository.updateOtherUsersStatusOnLocal(loggedInUserFirebaseId, otherUsersStatus)
    }
}