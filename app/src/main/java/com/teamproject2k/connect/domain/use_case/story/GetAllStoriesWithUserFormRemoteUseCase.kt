package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetAllStoriesWithUserFormRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<StoriesWithUserBean>> {
        return repository.getAllStoriesWithUserDetailsFromRemote(loggedInUserFirebaseId)
    }
}