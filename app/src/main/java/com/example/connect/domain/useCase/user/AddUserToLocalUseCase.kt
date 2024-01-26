package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
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