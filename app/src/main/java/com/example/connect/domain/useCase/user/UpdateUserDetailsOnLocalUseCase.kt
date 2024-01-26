package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateUserDetailsOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Updates the user details on the local database.
     *
     * @param fieldsToUpdate The fields to update.
     * @param firebaseUserId The user's firebase ID.
     * @return The ID of the updated user.
     */
    suspend fun invoke(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): Long {
        return repository.updateUserDetailsOnLocal(fieldsToUpdate, firebaseUserId)
    }
}