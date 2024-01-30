package com.example.connect.domain.repository

import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String)

    suspend fun sendChatMessage(message: ChatBean): ResponseState<String>
}