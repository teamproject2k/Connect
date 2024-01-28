package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetAllStoriesWithUserFormRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUserBean>> {
        return repository.getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId)
    }
}