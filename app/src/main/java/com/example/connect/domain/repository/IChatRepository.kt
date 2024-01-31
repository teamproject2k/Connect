package com.example.connect.domain.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState
import com.google.firebase.database.ChildEventListener

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String)

    fun liveObserveChat(
        loggedInUserFirebaseId: String,
        otherUserFirebaseId: String,
        chatListState: SnapshotStateList<ChatBean>,
        onError: (errorMessage: String) -> Unit
    ): ChildEventListener

    fun removeEventListener(eventListener: ChildEventListener)

    suspend fun sendChatMessageOnRemote(message: ChatBean): ResponseState<Nothing>

    suspend fun updateMessageOnRemote(
        deletedBy:String,
        messageId: String
    ): ResponseState<Nothing>

}