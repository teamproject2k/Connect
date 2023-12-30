package com.example.connect.domain.useCase.fcm

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateFCMTokenOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(currentUserFirebaseId: String, fcmToken: String): ResponseState<Nothing> {
        return repository.updateFCMTokenOnRemote(currentUserFirebaseId, fcmToken)
    }
}