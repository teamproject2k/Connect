package com.example.connect.domain.useCase.story

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddUserToSeenListInRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyId: String, loggedInUserFireBaseId: String): ResponseState<Nothing> {
        return repository.addUserToSeenListInRemote(storyId, loggedInUserFireBaseId)
    }
}