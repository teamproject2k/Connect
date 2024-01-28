package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StorySeenListWithUserDetailsBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetSeenListFromRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenListWithUserDetailsBean>> {
        return repository.getSeenListFromRemote(storyId, loggedInUserFirebaseId)
    }
}
