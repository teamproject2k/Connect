package com.teamproject2k.connect.domain.useCase.fcm

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateFCMTokenOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    /**
     * Updates the user's FCM token on the remote server.
     *
     * @param loggedInUserFirebaseId The user's Firebase ID.
     * @param fcmToken The user's FCM token.
     * @return A [ResponseState] containing the result of the operation i.e success or failure.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String, fcmToken: String): ResponseState<Nothing> {
        return repository.updateFCMTokenOnRemote(loggedInUserFirebaseId, fcmToken)
    }
}