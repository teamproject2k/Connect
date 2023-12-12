package com.example.connect.data.repository

import com.example.connect.common.FirebaseConstants
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.post.PostRemoteEntity
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IPostRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IPostRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
) : IPostRepository {

    override suspend fun getPostDetailsFromDb(fireBaseId: String): List<PostBean> {
        // Get the post details from the local database.
        return appDatabase.getPostDao().getPostList(fireBaseId).map { it.toPostBean() }
    }

    override suspend fun getPostDetailsFromRemote(fireBaseId: String): ResponseState<List<PostBean>> {
        // Get the post details from the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.PostsKey)
                .whereEqualTo(PostRemoteEntity::fireBaseUserId.name, fireBaseId).get().await()
            val postList = arrayListOf<PostBean>()
            response.documents.forEach { document ->
                val post = document.toObject(PostRemoteEntity::class.java)
                if (post != null) {
                    postList.add(post.toPostBean(document.id))
                }
            }
            ResponseState.success(postList)
        } catch (exception: Exception) {
            // An error occurred while getting the post details from the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addPostToDb(postDetails: PostBean): Long {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPost(postDetails.toPostDbEntity())
    }

    override suspend fun addPostListToDb(postDetailList: List<PostBean>): LongArray {
        // Add the post details to the local database.
        return appDatabase.getPostDao().insertPostList(postDetailList.map { it.toPostDbEntity() })
    }

    override suspend fun uploadPostToRemote(
        postDetails: PostBean,
        fireBaseId: String
    ): ResponseState<String> {
        // Upload the post details to the server.
        return try {
            val response = fireStore.collection(FirebaseConstants.MediaKey).document(fireBaseId)
                .collection(FirebaseConstants.PostsKey).add(postDetails.toPostRemoteEntity())
                .await()
            ResponseState.success(response.id)
        } catch (exception: Exception) {
            // An error occurred while uploading the post details to the server.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}