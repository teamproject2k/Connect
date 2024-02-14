package com.teamproject2k.connect.domain.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.models.UserWithChatListBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState

interface IChatRepository {

    suspend fun getChatListFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>>


    suspend fun deleteAllChats(): Int


    suspend fun deleteChat(chatBean: ChatBean): Int

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

    suspend fun updateChatOnLocal(chatBean: ChatBean): Int
}