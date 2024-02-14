package com.teamproject2k.connect.domain.useCase.story

import com.teamproject2k.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetSeenListFromRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>> {
        return repository.getSeenListFromRemote(storyId, loggedInUserFirebaseId)
    }
}
