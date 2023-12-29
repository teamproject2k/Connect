package com.example.connect.domain.repository

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.network_request_response.ResponseState

interface IStoryRepository {
    suspend fun addStory(story: StoryBean): ResponseState<Nothing>
}