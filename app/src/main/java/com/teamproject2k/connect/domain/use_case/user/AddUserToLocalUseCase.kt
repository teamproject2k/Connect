package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class AddUserToLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the repository to add a user to the local database.
     *
     * @param userDetails The user details to be added to the local database.
     * @return The ID of the user that was added to the local database.
     */
    suspend operator fun invoke(userDetails: UsersBean): Long {
        return repository.addUserToLocal(userDetails)
    }
}