package com.example.connect.data.repository

import com.example.connect.data.local_db.AppDatabase
import com.example.connect.data.models.story.StoryRemoteEntity
import com.example.connect.data.models.user.UserRemoteEntity
import com.example.connect.domain.enums.StatusWithCurrentUserRemoteEnum
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IStoryRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore, private val appDatabase: AppDatabase
) :
    IStoryRepository {
    override suspend fun addStoryToRemote(story: StoryBean): ResponseState<Nothing> {
        return try {
            fireStore.collection(FirebaseConstants.STORY_KEY).document()
                .set(story.toStoryRemoteEntity()).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun addStoryToDb(story: StoryBean): Long {
        // Add the story to the local database.
        return appDatabase.getStoryDao().insertStory(story.toStoryDbEntity())
    }

    override suspend fun getAllStoriesWithUserDetailsFromRemote(currentUserFirebaseId: String): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, MutableList<UsersBean>>> {
        return try {
            val storyListResponse = fireStore.collection(FirebaseConstants.STORY_KEY)
                .orderBy(StoryRemoteEntity::createdAt.name, Query.Direction.DESCENDING)
                .get().await()
            val storyList = arrayListOf<StoryBean>()
            val userList = arrayListOf<UsersBean>()
            val currentUserDocument = fireStore.collection(FirebaseConstants.USER_KEY)
                .document(currentUserFirebaseId).get().await()
            val currentUser = currentUserDocument.toObject(UserRemoteEntity::class.java)
            if (currentUserDocument != null && currentUserDocument.exists() && currentUser != null) {
                userList.add(currentUser.toUserBean())
                storyListResponse.documents.forEach { document ->
                    if (document.exists()) {
                        val story = document.toObject(StoryRemoteEntity::class.java)
                        if (story != null) {
                            storyList.add(story.toStoryBean())
                        }
                    }
                }

                storyList.forEach { story ->
                    val isUserPresent =
                        userList.find { it.firebaseUserId == story.fireBaseUserId } != null
                    if (!isUserPresent) {
                        val user = fireStore.collection(FirebaseConstants.USER_KEY)
                            .document(story.fireBaseUserId)
                            .get()
                            .await()
                        if (user.exists()) {
                            val userDetails = user.toObject(UserRemoteEntity::class.java)
                            if (userDetails != null) {
                                val whetherShowStory =
                                    (story.fireBaseUserId == currentUser.firebaseUserId) ||
                                            (userDetails.otherUsersStatus[currentUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name)
                                if (!whetherShowStory) {
                                    storyList.remove(story)
                                }
                                userList.add(userDetails.toUserBean())
                            } else {
                                storyList.remove(story)
                            }
                        } else {
                            storyList.remove(story)
                        }
                    }
                }

                userList.forEach { user ->
                    val isStoryPresentForUser =
                        storyList.find { it.fireBaseUserId == user.firebaseUserId } != null
                    if (!isStoryPresentForUser) {
                        userList.remove(user)
                    }
                }

                val storiesPerUser = mutableMapOf<String, ArrayList<StoryBean>>()

                storyList.forEach { story ->
                    val storyPoster = userList.find { it.firebaseUserId == story.fireBaseUserId }
                    if (storyPoster != null) {
                        val userStories =
                            storiesPerUser.getOrPut(storyPoster.firebaseUserId) { arrayListOf() }
                        userStories.add(story)
                        storiesPerUser[storyPoster.firebaseUserId] = userStories
                    }
                }

                ResponseState.success(Pair(storiesPerUser, userList))
            } else {
                ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}