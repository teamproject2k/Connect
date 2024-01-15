package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateFcmTokenOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {

    /**
     * Updates the FCM token on the local database.
     *
     * @param loggedInUserFirebaseId The current user's Firebase ID.
     * @param updatedToken The updated FCM token.
     * @return The number of rows affected.
     */
    suspend fun invoke(loggedInUserFirebaseId: String, updatedToken: String): Int {
        return repository.updateFCMTokenOnLocal(loggedInUserFirebaseId, updatedToken)
    }
}