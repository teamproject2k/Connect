package com.example.connect.domain.repository

import com.example.connect.domain.models.ChatBean

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String)

    suspend fun sendChatMessage(message: ChatBean)
}