package com.example.connect.data.repository

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IStoryRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
    IStoryRepository {
    override suspend fun addStory(story: StoryBean): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.STORY_KEY).document()
                .set(story.toStoryRemoteEntity()).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }
}