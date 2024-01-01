package com.example.connect.domain.useCase.story

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend fun invoke(storyId: String): ResponseState<Nothing> {
        return repository.deleteStoryInRemote(storyId)
    }
}