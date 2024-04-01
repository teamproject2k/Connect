package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToRemoteUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to add a story to the remote repository.
     *
     * @param storyDetails The [StoryBean] object representing the details of the story to be added.
     * @return A [ResponseState] containing a string representing the result of the addition operation.
     */
    suspend operator fun invoke(storyDetails: StoryBean): ResponseState<String> {
        return repository.addStoryToRemote(storyDetails)
    }
}