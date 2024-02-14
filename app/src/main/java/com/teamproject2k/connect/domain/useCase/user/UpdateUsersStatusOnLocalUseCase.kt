package com.teamproject2k.connect.domain.useCase.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUsersStatusOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to update the other users' status on the database.
     *
     * @param loggedInUserFirebaseId The current user's Firebase ID.
     * @param otherUsersStatus A mutable map of other users' Firebase IDs to their status.
     * @return The number of users whose status was successfully updated.
     */
    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        otherUsersStatus: MutableMap<String, String>
    ): Int {
        return repository.updateUsersStatusOnLocal(loggedInUserFirebaseId, otherUsersStatus)
    }
}