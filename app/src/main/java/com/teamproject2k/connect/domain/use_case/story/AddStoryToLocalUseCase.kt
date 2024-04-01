package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to add a story to the local repository.
     *
     * @param storyDetails The [StoryBean] object representing the details of the story to be added.
     * @return A long value representing the ID of the newly added story.
     */
    suspend operator fun invoke(storyDetails: StoryBean): Long {
        return repository.addStoryToLocal(storyDetails)
    }
}