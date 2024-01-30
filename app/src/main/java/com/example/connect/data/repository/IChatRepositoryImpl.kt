package com.example.connect.data.repository

import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IChatRepository
import com.example.connect.domain.utils.FirebaseConstants
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

    override fun liveObserveChat(loggedInUserFirebaseId: String, otherUserFirebaseId: String) {
        val resultId =
            if (loggedInUserFirebaseId < otherUserFirebaseId) loggedInUserFirebaseId + otherUserFirebaseId else otherUserFirebaseId + loggedInUserFirebaseId

        firebaseDatabase.reference.child(FirebaseConstants.CHATS_KEY).child(resultId).addValueEventListener {

        }
    }

    override suspend fun sendChatMessage(message: ChatBean): ResponseState<Nothing> {
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
}