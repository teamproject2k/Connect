package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.story.StoryRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.models.StoriesWithUser
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IStoryRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val appDatabase: AppDatabase
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

    override suspend fun getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUser>> {
        return try {
            val loggedInUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            val userList = arrayListOf<UsersBean>()
            val storyList = arrayListOf<StoryBean>()
            val useIdToStoryListMap = mutableMapOf<UsersBean, ArrayList<StoryBean>>()
            val loggedInUser =
                loggedInUserDocument.toObject(UserRemoteEntity::class.java)?.toUserBean()
            if (loggedInUserDocument != null && loggedInUserDocument.exists() && loggedInUser != null) {
                userList.add(loggedInUser)
                val getStoriesFor = loggedInUser.friendList
                getStoriesFor.add(loggedInUserFirebaseId)
                val currentTimeMillis = System.currentTimeMillis()
                val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
                val elapsedTimeInMillis = currentTimeMillis - twentyFourHoursInMillis
                val storyListResponse = fireStore.collection(FirebaseConstants.STORY_KEY)
                    .whereIn(
                        StoryRemoteEntity::createdByUserFirebaseId.name,
                        getStoriesFor
                    ).whereEqualTo(StoryRemoteEntity::whetherDeleted.name, false)
                    .whereGreaterThan(StoryRemoteEntity::createdAt.name, elapsedTimeInMillis).get()
                    .await()
                storyListResponse.forEach { storyDocument ->
                    val story = storyDocument.toObject(StoryRemoteEntity::class.java)
                    storyList.add(story.toStoryBean(storyDocument.id))
                }
                if (storyList.any { it.createdByUserFirebaseId == loggedInUserFirebaseId }) {
                    useIdToStoryListMap[loggedInUser] = arrayListOf()
                }
                val allStoryPostersIdList =
                    storyList.map { it.createdByUserFirebaseId }.toSet().toList()
                if (allStoryPostersIdList.isNotEmpty()) {
                    val allUsersDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                        .whereIn(UserRemoteEntity::firebaseUserId.name, allStoryPostersIdList).get()
                        .await()
                    allUsersDocument.documents.forEach { document ->
                        if (document != null && document.exists()) {
                            val user = document.toObject(UserRemoteEntity::class.java)
                            if (user != null) {
                                userList.add(user.toUserBean())
                            }
                        }
                    }
                    storyList.forEach { story ->
                        if (story.createdByUserFirebaseId != loggedInUserFirebaseId) {
                            val storyPoster =
                                userList.find { it.firebaseUserId == story.createdByUserFirebaseId }
                            if (storyPoster != null && storyPoster.friendList.contains(
                                    loggedInUserFirebaseId
                                )
                            ) {
                                if (useIdToStoryListMap.containsKey(storyPoster)) {
                                    useIdToStoryListMap[storyPoster]?.add(story)
                                } else {
                                    useIdToStoryListMap[storyPoster] =
                                        arrayListOf(story)
                                }
                            }
                        }
                    }
                    ResponseState.success(useIdToStoryListMap.map {
                        StoriesWithUser(
                            it.key,
                            it.value
                        )
                    } as ArrayList)
                } else {
                    ResponseState.success(arrayListOf())
                }
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
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

    override suspend fun getAllStoriesFromLocal(): List<StoryBean> {
        return appDatabase.getStoryDao().getAllStories().map { it.toStoryBean() }
    }

    override suspend fun addAllStoriesToLocal(storyList: List<StoryBean>): LongArray {
        return appDatabase.getStoryDao().insertAllStories(storyList.map { it.toStoryDbEntity() })
    }

    override suspend fun deleteAllStoriesFromLocal(): Int {
        return appDatabase.getStoryDao().deleteAllStories()
    }

    override suspend fun deleteStoryFromLocal(storyId: String): Int {
        return appDatabase.getStoryDao().deleteStory(storyId)
    }

    override suspend fun addStoryToLocal(story: StoryBean): Long {
        return appDatabase.getStoryDao().insertStory(story.toStoryDbEntity())
    }

}