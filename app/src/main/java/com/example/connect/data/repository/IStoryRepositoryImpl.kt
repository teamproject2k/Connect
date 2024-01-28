package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.story.StoryRemoteEntity
import com.example.connect.data.models.story.StorySeenByRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.google.firebase.firestore.FieldValue
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

    override suspend fun getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUserBean>> {
        return try {
            val loggedInUserDocument =
                fireStore.collection(FirebaseConstants.USER_KEY).document(loggedInUserFirebaseId)
                    .get().await()
            val userList = arrayListOf<UsersBean>()
            val storyList = arrayListOf<StoryBean>()
            val userToStoryListMap = mutableMapOf<UsersBean, ArrayList<StoryBean>>()
            val loggedInUser =
                loggedInUserDocument.toObject(UserRemoteEntity::class.java)?.toUserBean()
            if (loggedInUser != null) {
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
                    .whereGreaterThan(StoryRemoteEntity::createdAt.name, elapsedTimeInMillis)
                    .get()
                    .await()
                storyListResponse.forEach { storyDocument ->
                    val story = storyDocument.toObject(StoryRemoteEntity::class.java)
                    storyList.add(story.toStoryBean(storyDocument.id))
                }
                if (storyList.any { it.createdByUserFirebaseId == loggedInUserFirebaseId }) {
                    userToStoryListMap[loggedInUser] = arrayListOf()
                }
                val allStoryPostersIdList =
                    storyList.map { it.createdByUserFirebaseId }.toSet().toList()
                if (allStoryPostersIdList.isNotEmpty()) {
                    val allUsersDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                        .whereIn(UserRemoteEntity::firebaseUserId.name, allStoryPostersIdList).get()
                        .await()
                    allUsersDocument.documents.forEach { document ->
                        val user = document.toObject(UserRemoteEntity::class.java)
                        if (user != null) {
                            userList.add(user.toUserBean())
                        }
                    }
                    storyList.sortByDescending { it.createdAt }
                    storyList.forEach { story ->
                        val storyPoster =
                            userList.find { it.firebaseUserId == story.createdByUserFirebaseId }
                        if (storyPoster != null && (storyPoster.firebaseUserId == loggedInUserFirebaseId || storyPoster.friendList.contains(
                                loggedInUserFirebaseId
                            ))
                        ) {
                            if (userToStoryListMap.containsKey(storyPoster)) {
                                userToStoryListMap[storyPoster]?.add(story)
                            } else {
                                userToStoryListMap[storyPoster] =
                                    arrayListOf(story)
                            }
                        }
                    }

                    val storyListWithUserDetails = userToStoryListMap.map {
                        it.value.reverse()
                        StoriesWithUserBean(
                            it.key,
                            it.value
                        )
                    } as ArrayList
                    ResponseState.success(storyListWithUserDetails)
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
        storySeenBy: String,
        storySeenAt: Long
    ): ResponseState<Nothing> {
        return try {
            // Get the reference to the story document in the FireStore database.
            val storyDocumentReference =
                fireStore.collection(FirebaseConstants.STORY_KEY).document(storyId)

            // Update the story document by adding the loggedInUserFirebaseId to the seenList
            storyDocumentReference.update(
                StoryRemoteEntity::seenBy.name,
                FieldValue.arrayUnion(StorySeenByRemoteEntity(storySeenBy, storySeenAt))
            ).await()

            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun getSeenListFromRemote(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>> {
        return try {
            val storyDocument =
                fireStore.collection(FirebaseConstants.STORY_KEY).document(storyId).get().await()
            val story = storyDocument.toObject(StoryRemoteEntity::class.java)
            val seenList = arrayListOf<StorySeenTimeWithUserDetailsBean>()
            if (story != null && story.seenBy.isNotEmpty() && !story.whetherDeleted) {
                val seenUserListDocument = fireStore.collection(FirebaseConstants.USER_KEY).whereIn(
                    UserRemoteEntity::firebaseUserId.name,
                    story.seenBy.map { it.seenUserId }).get().await()
                seenUserListDocument.forEach { userDocument ->
                    val user = userDocument.toObject(UserRemoteEntity::class.java)
                    val seenTime =
                        story.seenBy.find { it.seenUserId == user.firebaseUserId }?.seenTime
                    if (seenTime != null && user.otherUsersStatus[loggedInUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name) {
                        seenList.add(StorySeenTimeWithUserDetailsBean(user.toUserBean(), seenTime))
                    }
                }
            }
            seenList.sortByDescending { it.seenAt }
            ResponseState.success(seenList)
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