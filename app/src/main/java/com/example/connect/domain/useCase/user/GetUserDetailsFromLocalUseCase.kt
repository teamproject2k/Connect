package com.example.connect.domain.useCase.user

import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserDetailsFromLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Gets the user details from the database.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return The user details, or null if the user does not exist.
     */
    suspend operator fun invoke(fireBaseId: String): UsersBean? {
        return repository.getUserDetailsFromLocal(fireBaseId)
    }
}