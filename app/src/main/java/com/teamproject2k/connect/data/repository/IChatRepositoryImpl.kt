package com.teamproject2k.connect.data.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.teamproject2k.connect.data.local_db.AppDatabase
import com.teamproject2k.connect.data.models.chats.ChatRemoteEntity
import com.teamproject2k.connect.data.models.user.UserRemoteEntity
import com.teamproject2k.connect.data.utils.DataFunctionHelper
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.models.UserWithChatListBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import com.teamproject2k.connect.domain.utils.FirebaseConstants
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IChatRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) :
    IChatRepository {
    override suspend fun getChatListFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>> {
        return try {
            val chatListSnapshotStartAtLoggedInUser =
                firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY)
                    .startAt(loggedInUserFirebaseId)
                    .get().await()
            val chatListSnapshotEndAtLoggedInUser =
                firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY)
                    .endAt(loggedInUserFirebaseId)
                    .get().await()
            val loggedInUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            val loggedInUserBean =
                loggedInUserDocument.toObject(UserRemoteEntity::class.java)?.toUserBean()
            val userWithChatList = arrayListOf<UserWithChatListBean>()
            if (loggedInUserBean != null) {
                userWithChatList.addAll(
                    (getChatData(
                        chatListSnapshotStartAtLoggedInUser,
                        loggedInUserFirebaseId
                    ))
                )
                userWithChatList.addAll(
                    (getChatData(
                        chatListSnapshotEndAtLoggedInUser,
                        loggedInUserFirebaseId
                    ))
                )
            }
            ResponseState.success(userWithChatList)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun deleteAllChats(): Int {
        return appDatabase.getChatDao().deleteAllChats()
    }

    override suspend fun deleteChat(chatBean: ChatBean): Int {
        return appDatabase.getChatDao().deleteChat(chatBean.toChatLocalEntity())
    }

    private suspend fun getChatData(
        chatListSnapshot: DataSnapshot,
        loggedInUserFirebaseId: String
    ): ArrayList<UserWithChatListBean> {
        val userWithChatList = arrayListOf<UserWithChatListBean>()
        chatListSnapshot.children.forEach { snapshot ->
            val chatKey = snapshot.key
            val otherUserFirebaseId = chatKey?.replace(loggedInUserFirebaseId, "")
            if (otherUserFirebaseId != null) {
                val otherUserSnapshot =
                    fireStore.collection(FirebaseConstants.USER_KEY)
                        .document(otherUserFirebaseId)
                        .get().await()
                val otherUserBean =
                    otherUserSnapshot.toObject(UserRemoteEntity::class.java)?.toUserBean()
                if (otherUserBean != null) {
                    val chatList = arrayListOf<ChatBean>()
                    snapshot.children.forEach { chatSnapshot ->
                        val chatMessage = chatSnapshot.key?.let {
                            chatSnapshot.getValue(ChatRemoteEntity::class.java)?.toChatBean(
                                it
                            )
                        }
                        if (chatMessage != null && !DataFunctionHelper.whetherNotToShowChatToLoggedInUser(
                                chatMessage.deletedBy,
                                chatMessage.senderId,
                                chatMessage.receiverId,
                                loggedInUserFirebaseId
                            )
                        ) {
                            chatList.add(chatMessage)
                        }
                    }
                    chatList.sortBy { it.sentAt }
                    userWithChatList.add(UserWithChatListBean(otherUserBean, chatList))
                }
            }
        }
        return userWithChatList
    }

    override fun liveObserveChat(
        loggedInUserFirebaseId: String,
        otherUserFirebaseId: String,
        chatListState: SnapshotStateList<ChatBean>,
        onError: (errorMessage: String) -> Unit
    ): ChildEventListener {
        val resultId =
            if (loggedInUserFirebaseId < otherUserFirebaseId) loggedInUserFirebaseId + otherUserFirebaseId else otherUserFirebaseId + loggedInUserFirebaseId
        return firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY).child(resultId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatRemoteEntity = snapshot.getValue(ChatRemoteEntity::class.java)
                    val chatDocumentId = snapshot.key
                    if (chatRemoteEntity != null && chatDocumentId != null && !DataFunctionHelper.whetherNotToShowChatToLoggedInUser(
                            chatRemoteEntity.deletedBy,
                            chatRemoteEntity.senderId,
                            chatRemoteEntity.receiverId,
                            loggedInUserFirebaseId
                        )
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            appDatabase.getChatDao()
                                .insertMessage(
                                    chatRemoteEntity.toChatBean(chatDocumentId).toChatLocalEntity()
                                )
                            appDatabase.getChatMetaDataDao().updateChatListLastSeen(
                                DomainFunctionHelper.getSortedChatId(
                                    loggedInUserFirebaseId,
                                    otherUserFirebaseId
                                ), FunctionHelper.getCurrentTimeInMillis()
                            )
                        }
                        chatListState.add(chatRemoteEntity.toChatBean(chatDocumentId))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatRemoteEntity = snapshot.getValue(ChatRemoteEntity::class.java)
                    val chatDocumentId = snapshot.key
                    if (chatRemoteEntity != null && chatDocumentId != null) {
                        chatListState.removeIf { it.firebaseId == chatDocumentId }
                        CoroutineScope(Dispatchers.IO).launch {
                            if (!DataFunctionHelper.whetherNotToShowChatToLoggedInUser(
                                    chatRemoteEntity.deletedBy,
                                    chatRemoteEntity.senderId,
                                    chatRemoteEntity.receiverId,
                                    loggedInUserFirebaseId
                                )
                            ) {
                                appDatabase.getChatDao()
                                    .insertMessage(
                                        chatRemoteEntity.toChatBean(chatDocumentId)
                                            .toChatLocalEntity()
                                    )
                                chatListState.add(chatRemoteEntity.toChatBean(chatDocumentId))
                            } else {
                                appDatabase.getChatDao().deleteChat(
                                    chatRemoteEntity.toChatBean(chatDocumentId).toChatLocalEntity()
                                )
                            }
                            appDatabase.getChatMetaDataDao().updateChatListLastSeen(
                                DomainFunctionHelper.getSortedChatId(
                                    loggedInUserFirebaseId,
                                    otherUserFirebaseId
                                ), FunctionHelper.getCurrentTimeInMillis()
                            )
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val chatDocumentId = snapshot.key
                    if (chatDocumentId != null) {
                        chatListState.removeIf { it.firebaseId == chatDocumentId }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // no need to handled it
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }

            })
    }

    override fun removeEventListener(eventListener: ChildEventListener) {
        firebaseDatabase.reference.removeEventListener(eventListener)
    }

    override suspend fun sendChatMessageOnRemote(message: ChatBean): ResponseState<Nothing> {
        return try {
            val id1 = message.senderId
            val id2 = message.receiverId
            val resultId = if (id1 < id2) id1 + id2 else id2 + id1
            firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY).child(resultId).push()
                .setValue(message.toChatRemoteEntity()).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun deleteMessageOnRemote(
        deletedBy: String,
        senderId: String,
        receiverId: String,
        messageId: String
    ): ResponseState<Nothing> {
        return try {
            val resultId = DomainFunctionHelper.getSortedChatId(senderId, receiverId)
            firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY).child(resultId)
                .child(messageId)
                .child(ChatRemoteEntity::deletedBy.name).setValue(deletedBy)
            // Return a success response.
            ResponseState.success(null)
        } catch (exception: Exception) {
            // Return an error response if an exception occurs.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addChatMetaDataToLocal(chatMetaDataList: List<ChatMetaDataBean>): LongArray {
        return appDatabase.getChatMetaDataDao()
            .insertChatMetaDataList(chatMetaDataList.map { it.toChatMetaDataLocalEntity() })
    }

    override suspend fun addChatListToLocal(chatList: List<ChatBean>): LongArray {
        return appDatabase.getChatDao()
            .insertChatMessagesList(chatList.map { it.toChatLocalEntity() })
    }

    override suspend fun getUserWithLastMessageWithUnreadCount(loggedInUserFirebaseId: String): List<ChatWithUserAndCountBean> {
        val chatMetaDataList = appDatabase.getChatMetaDataDao().getAllChatMetaDatList()
        val chatWithUserAndCountList = arrayListOf<ChatWithUserAndCountBean>()
        val usersIdList = chatMetaDataList.map { it.chatId.replace(loggedInUserFirebaseId, "") }
        val usersList = appDatabase.getUsersDao().getAllUserFromIds(usersIdList)
        chatMetaDataList.forEach { chatMetaData ->
            val user = usersList.find {
                it.firebaseUserId == chatMetaData.chatId.replace(
                    loggedInUserFirebaseId,
                    ""
                )
            }
            val lastMessage =
                appDatabase.getChatDao().getLastMessage(chatMetaData.chatId)
            val unreadMessageCount = appDatabase.getChatDao()
                .getUnreadMessageCount(
                    chatMetaData.chatId,
                    chatMetaData.lastSeenChatAt,
                )
            if (user != null && lastMessage != null) {
                chatWithUserAndCountList.add(
                    ChatWithUserAndCountBean(
                        user.toUserBean(),
                        unreadMessageCount,
                        lastMessage.toChatBean()
                    )
                )
            }
        }
        return chatWithUserAndCountList
    }

    override suspend fun updateLastSeenAtOnLocal(chatId: String, lastSeenAt: Long): Int {
        return appDatabase.getChatMetaDataDao().updateChatListLastSeen(chatId, lastSeenAt)
    }

    override suspend fun updateChatOnLocal(chatBean: ChatBean): Int {
        return appDatabase.getChatDao().updateMessage(chatBean.toChatLocalEntity())
    }
}