package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.story.StoryRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IStoryRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore, private val appDatabase: AppDatabase
) :
    IStoryRepository {
    override suspend fun addStoryToRemote(story: StoryBean): ResponseState<String> {
        return try {
            val response = fireStore.collection(FirebaseConstants.STORY_KEY)
                .add(story.toStoryRemoteEntity()).await()
            ResponseState.success(response.id)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    private fun isUploadedBeforeOneDay(createdAtInMillis: Long): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

        val elapsedTimeInMillis = currentTimeMillis - createdAtInMillis
        return elapsedTimeInMillis < twentyFourHoursInMillis
    }

    override suspend fun getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId: String): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>> {
        return try {
            val storyListResponse = fireStore.collection(FirebaseConstants.STORY_KEY)
                .whereEqualTo(StoryRemoteEntity::whetherDeleted.name, false).get().await()
            val storyList = arrayListOf<StoryBean>()
            val userList = arrayListOf<UsersBean>()
            val storiesPerUser = mutableMapOf<String, ArrayList<StoryBean>>()
            storyListResponse.documents.forEach { document ->
                if (document.exists()) {
                    val story = document.toObject(StoryRemoteEntity::class.java)
                    val isUploadedBeforeOneDay = isUploadedBeforeOneDay(story?.createdAt ?: 0)
                    if (story != null && isUploadedBeforeOneDay) {
                        storyList.add(story.toStoryBean(document.id))
                    }
                }
            }
            storyList.sortBy { it.createdAt }
            if (storyList.isNotEmpty()) {
                val userListIds = storyList.map { it.fireBaseUserId }.toSet().toList()
                val userListResponse = fireStore.collection(FirebaseConstants.USER_KEY)
                    .whereIn(UserRemoteEntity::firebaseUserId.name, userListIds).get().await()
                userListResponse.documents.forEach { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(UserRemoteEntity::class.java)
                        if (user != null) {
                            userList.add(user.toUserBean())
                        }
                    }
                }
                storyList.removeIf { story ->
                    val user = userList.find { it.firebaseUserId == story.fireBaseUserId }
                    val whetherShowStory =
                        story.fireBaseUserId == loggedInUserFirebaseId ||
                                (user?.friendList?.contains(loggedInUserFirebaseId) == true)
                    if (!whetherShowStory) {
                        userList.remove(user)
                    }
                    !whetherShowStory || user == null
                }
                val currentUserStories =
                    storyList.filter { it.fireBaseUserId == loggedInUserFirebaseId } as ArrayList
                if (currentUserStories.isNotEmpty()) {
                    storiesPerUser[loggedInUserFirebaseId] = currentUserStories
                }
                storyList.forEach { story ->
                    if (story.fireBaseUserId != loggedInUserFirebaseId) {
                        val storyPoster =
                            userList.find { it.firebaseUserId == story.fireBaseUserId }
                        if (storyPoster != null) {
                            val userStories =
                                storiesPerUser.getOrPut(storyPoster.firebaseUserId) { arrayListOf() }
                            userStories.add(story)
                            storiesPerUser[storyPoster.firebaseUserId] = userStories
                        }
                    }
                }
            }
            ResponseState.success(Pair(storiesPerUser, userList))
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addUserToSeenListInRemote(
        storyId: String,
        loggedInUserFireBaseId: String
    ): ResponseState<Nothing> {
        return try {
            // Get the reference to the story document in the FireStore database.
            val storyDocumentReference =
                fireStore.collection(FirebaseConstants.STORY_KEY).document(storyId)

            val listItem = hashMapOf(loggedInUserFireBaseId to System.currentTimeMillis())

            // Update the story document by adding the loggedInUserFirebaseId to the seenList
//            storyDocumentReference.update(
//                StoryRemoteEntity::seenList.name,
//                FieldValue.arrayUnion(listItem)
//            ).await()

            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getSeenListFromRemote(storyId: String): ResponseState<List<Pair<String, Long>>> {
        return try {
            val storyDocumentReference =
                fireStore.collection(FirebaseConstants.STORY_KEY).document(storyId)
            val documentSnapshot: DocumentSnapshot = storyDocumentReference.get().await()

            if (documentSnapshot.exists()) {
//                val seenList =
//                    documentSnapshot[StoryRemoteEntity::seenList.name] as? List<Pair<String, Long>>
                ResponseState.success(emptyList())
            } else {
                ResponseState.success(null) // Return null if the document doesn't exist or doesn't contain the seenList field
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun deleteStoryInRemote(storyId: String): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.STORY_KEY).document(storyId)
                .update(StoryRemoteEntity::whetherDeleted.name, true).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}