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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    override suspend fun addStoryToDb(story: StoryBean): Long {
        // Add the story to the local database.
        return appDatabase.getStoryDao().insertStory(story.toStoryDbEntity())
    }

    override suspend fun getAllStoriesWithUserDetailsFromRemote(currentUserFirebaseId: String): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>> {
        return try {
            val storyListResponse = fireStore.collection(FirebaseConstants.STORY_KEY)
                .orderBy(StoryRemoteEntity::createdAt.name, Query.Direction.DESCENDING)
                .get().await()
            val storyList = arrayListOf<StoryBean>()
            val userList = arrayListOf<UsersBean>()
            storyListResponse.documents.forEach { document ->
                if (document.exists()) {
                    val story = document.toObject(StoryRemoteEntity::class.java)
                    if (story != null) {
                        storyList.add(story.toStoryBean(document.id))
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
                                story.fireBaseUserId == currentUserFirebaseId ||
                                        (userDetails.otherUsersStatus[currentUserFirebaseId] == StatusWithCurrentUserRemoteEnum.Friends.name)
                            if (!whetherShowStory) {
                                storyList.removeAll {
                                    it.fireBaseUserId == userDetails.firebaseUserId
                                }
                            } else {
                                userList.add(userDetails.toUserBean())
                            }
                        } else {
                            storyList.removeAll {
                                it.fireBaseUserId == story.fireBaseUserId
                            }
                        }
                    } else {
                        storyList.removeAll {
                            it.fireBaseUserId == story.fireBaseUserId
                        }
                    }
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
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}