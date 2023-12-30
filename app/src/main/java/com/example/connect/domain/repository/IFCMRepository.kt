package com.example.connect.domain.repository

import com.example.connect.domain.network_request_response.ResponseState

interface IFCMRepository {

    suspend fun getFCMToken(): ResponseState<String>


    suspend fun sendFCMMessage(
        token: String,
        data: Map<String, String>,
        sendTo: String
    ): ResponseState<Nothing>
}
