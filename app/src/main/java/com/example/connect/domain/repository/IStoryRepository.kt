package com.example.connect.domain.repository

import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.example.connect.domain.network_request_response.ResponseState

interface IStoryRepository {
    suspend fun addStoryToRemote(story: StoryBean): ResponseState<String>
    suspend fun getAllStoriesWithUserDetailsFromRemote(
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StoriesWithUserBean>>

    suspend fun addUserToSeenListInRemote(
        storyId: String,
        storySeenBy: String,
        storySeenAt: Long
    ): ResponseState<Nothing>

    suspend fun getSeenListFromRemote(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>>

    suspend fun deleteStoryInRemote(storyId: String): ResponseState<Nothing>

    suspend fun getAllStoriesFromLocal(): List<StoryBean>

    suspend fun addAllStoriesToLocal(storyList: List<StoryBean>): LongArray

    suspend fun deleteAllStoriesFromLocal(): Int

    suspend fun deleteStoryFromLocal(storyId: String): Int

    suspend fun addStoryToLocal(story: StoryBean): Long


    suspend fun updateStoryOnLocal(story: StoryBean): Int
}