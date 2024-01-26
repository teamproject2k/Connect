package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoriesWithUser
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetAllStoriesWithUserFormRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUser>> {
        return repository.getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId)
    }
}