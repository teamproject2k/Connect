package com.example.connect.domain.repository

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState

interface IStoryRepository {
    suspend fun addStoryToRemote(story: StoryBean): ResponseState<Nothing>
    suspend fun addStoryToDb(story: StoryBean): Long
    suspend fun getAllStoriesWithUserDetailsFromRemote(
        currentUserFirebaseId: String
    ): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, MutableList<UsersBean>>>
}