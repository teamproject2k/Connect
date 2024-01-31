package com.example.connect.data.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.connect.data.models.chats.ChatRemoteEntity
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IChatRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IChatRepositoryImpl @Inject constructor(private val firebaseDatabase: FirebaseDatabase) :
    IChatRepository {
    override suspend fun getChatListFromRemote(loggedInUserFirebaseId: String) {
//        try {
//            val chatListResponse =
//                fireStore.collection(FirebaseConstants.INTERACTIONS_KEY)
//                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), loggedInUserFirebaseId)
//                    .whereLessThanOrEqualTo(
//                        FieldPath.documentId(),
//                        loggedInUserFirebaseId + "\uf8ff"
//                    )
//                    .get()
//                    .await()
//
//
//            chatListResponse.forEach { chat ->
//
//            }
//        } catch (exception: Exception) {
//            ResponseState.error(exception.localizedMessage ?: "")
//        }
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
                    val whetherNotShowMessageToLoggedInUser =
                        chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForEveryone.name
                                || (chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForSender.name && chatRemoteEntity.senderId == loggedInUserFirebaseId)
                                || (chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForReceiver.name && chatRemoteEntity.receiverId == loggedInUserFirebaseId)

                    if (chatRemoteEntity != null && chatDocumentId != null && !whetherNotShowMessageToLoggedInUser) {
                        chatListState.add(chatRemoteEntity.toChatBean(chatDocumentId))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatRemoteEntity = snapshot.getValue(ChatRemoteEntity::class.java)
                    val chatDocumentId = snapshot.key
                    val whetherNotShowMessageToLoggedInUser =
                        chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForEveryone.name
                                || (chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForSender.name && chatRemoteEntity.senderId == loggedInUserFirebaseId)
                                || (chatRemoteEntity?.deletedBy == MessageDeleteStatusEnum.DeletedForReceiver.name && chatRemoteEntity.receiverId == loggedInUserFirebaseId)
                    if (chatRemoteEntity != null && chatDocumentId != null) {
                        chatListState.removeIf { it.firebaseId == chatDocumentId }
                        if (!whetherNotShowMessageToLoggedInUser) {
                            chatListState.add(chatRemoteEntity.toChatBean(chatDocumentId))
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
            val resultId =
                if (senderId < receiverId) senderId + receiverId else receiverId + senderId
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
}