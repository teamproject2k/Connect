package com.example.connect.domain.useCase

import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class GetUserDetailsFromDbUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Gets the user details from the database.
     *
     * @param fireBaseId The user's Firebase ID.
     * @return The user details, or null if the user does not exist.
     */
    suspend fun getUserDetailsFromDb(fireBaseId: String): UserDetails? {
        return repository.getUserDetailsFromLocal(fireBaseId)
    }
}