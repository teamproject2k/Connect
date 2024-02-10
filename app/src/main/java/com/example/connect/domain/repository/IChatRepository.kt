package com.example.connect.domain.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.ChatMetaDataBean
import com.example.connect.domain.models.ChatWithUserAndCountBean
import com.example.connect.domain.models.UserWithChatListBean
import com.example.connect.domain.network_request_response.ResponseState
import com.google.firebase.database.ChildEventListener

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>>

    fun liveObserveChat(
        loggedInUserFirebaseId: String,
        otherUserFirebaseId: String,
        chatListState: SnapshotStateList<ChatBean>,
        onError: (errorMessage: String) -> Unit
    ): ChildEventListener

    fun removeEventListener(eventListener: ChildEventListener)

    suspend fun sendChatMessageOnRemote(message: ChatBean): ResponseState<Nothing>

    suspend fun deleteMessageOnRemote(
        deletedBy: String,
        senderId: String,
        receiverId: String,
        messageId: String
    ): ResponseState<Nothing>

    suspend fun addChatMetaDataToLocal(chatMetaDataList: List<ChatMetaDataBean>): LongArray

    suspend fun addChatListToLocal(chatList: List<ChatBean>): LongArray

    suspend fun getUserWithLastMessageWithUnreadCount(loggedInUserFirebaseId: String): List<ChatWithUserAndCountBean>

    suspend fun updateLastSeenAtOnLocal(
        chatId: String,
        lastSeenAt: Long
    ): Int
}