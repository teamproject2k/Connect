package com.example.connect.domain.useCase

import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IAuthenticationRepository
import javax.inject.Inject

class AddUserToDbUseCase @Inject constructor(private val repository: IAuthenticationRepository) {

    /**
     * Invokes the repository to add a user to the local database.
     *
     * @param userDetails The user details to be added to the local database.
     * @return The ID of the user that was added to the local database.
     */
    suspend fun invoke(userDetails: UserDetails): Long {
        return repository.addUserToLocalDb(userDetails)
    }
}