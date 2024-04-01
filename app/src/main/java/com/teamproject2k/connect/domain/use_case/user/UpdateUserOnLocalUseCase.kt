package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Invokes the function to update user details in the local database.
     *
     * @param userDetails The details of the user to be updated.
     * @return The number of users updated in the local database.
     */
    suspend operator fun invoke(userDetails: UserBean): Int {
        return repository.updateUserOnLocal(userDetails)
    }
}