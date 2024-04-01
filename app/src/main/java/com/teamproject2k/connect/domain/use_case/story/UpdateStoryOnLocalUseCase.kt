package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class UpdateStoryOnLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to update a story in the local repository.
     *
     * @param storyBean The [StoryBean] object representing the story to be updated.
     * @return An integer representing the result of the update operation.
     */
    suspend operator fun invoke(storyBean: StoryBean): Int {
        return repository.updateStoryOnLocal(storyBean)
    }
}