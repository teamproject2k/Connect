package com.example.connect.domain.repository

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String)
}