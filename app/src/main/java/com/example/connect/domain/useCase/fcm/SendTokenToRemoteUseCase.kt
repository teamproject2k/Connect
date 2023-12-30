package com.example.connect.domain.useCase.fcm

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IFCMRepository
import javax.inject.Inject

class SendTokenToRemoteUseCase @Inject constructor(private val repository: IFCMRepository) {

    suspend fun invoke(currentUserFirebaseId: String, fcmToken: String): ResponseState<Nothing> {
        return repository.sendTokenToRemote(currentUserFirebaseId, fcmToken)
    }
}