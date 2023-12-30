package com.example.connect.domain.repository

import com.example.connect.domain.network_request_response.ResponseState

interface IFCMRepository {
    suspend fun sendTokenToRemote(
        currentUserFirebaseId: String,
        fcmToken: String
    ): ResponseState<Nothing>


    suspend fun getFCMToken(): ResponseState<String>
}
