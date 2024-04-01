package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddAllStoriesToLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to add a list of stories to the local repository.
     *
     * @param storiesList The list of [StoryBean] objects representing the stories to be added.
     * @return An array of long values representing the IDs of the newly added stories.
     */
    suspend operator fun invoke(storiesList: List<StoryBean>): LongArray {
        return repository.addAllStoriesToLocal(storiesList)
    }
}