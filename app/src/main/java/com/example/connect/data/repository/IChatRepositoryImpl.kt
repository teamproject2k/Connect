package com.example.connect.data.repository

import com.example.connect.domain.repository.IChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class IChatRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
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
}