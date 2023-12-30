package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend fun invoke(storyDetails: StoryBean): ResponseState<String> {
        return repository.addStoryToRemote(storyDetails)
    }
}