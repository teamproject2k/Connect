package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToDbUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Adds a story to the local database.
     *
     * @param story The story to add.
     * @return The ID of the story that was added.
     */
    suspend fun invoke(story: StoryBean): Long {
        return repository.addStoryToDb(story)
    }
}