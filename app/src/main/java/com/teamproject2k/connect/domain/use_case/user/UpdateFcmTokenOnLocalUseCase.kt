package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateFcmTokenOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    /**
     * Updates the FCM token on the local database.
     *
     * @param loggedInUserFirebaseId The current user's Firebase ID.
     * @param updatedToken The updated FCM token.
     * @return The number of rows affected.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String, updatedToken: String): Int {
        return repository.updateFCMTokenOnLocal(loggedInUserFirebaseId, updatedToken)
    }
}