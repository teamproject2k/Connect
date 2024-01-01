package com.example.connect.domain.repository

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState

interface IStoryRepository {
    suspend fun addStoryToRemote(story: StoryBean): ResponseState<String>
    suspend fun getAllStoriesWithUserDetailsFromRemote(
        currentUserFirebaseId: String
    ): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>>

    suspend fun addUserToSeenListInRemote(
        storyId: String,
        loggedInUserFireBaseId: String
    ): ResponseState<Nothing>

    suspend fun getSeenListFromRemote(storyId: String): ResponseState<List<Pair<String, Long>>>
    suspend fun deleteStoryInRemote(storyId: String): ResponseState<Nothing>
}