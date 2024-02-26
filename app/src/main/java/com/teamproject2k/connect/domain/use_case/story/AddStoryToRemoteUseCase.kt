package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyDetails: StoryBean): ResponseState<String> {
        return repository.addStoryToRemote(storyDetails)
    }
}