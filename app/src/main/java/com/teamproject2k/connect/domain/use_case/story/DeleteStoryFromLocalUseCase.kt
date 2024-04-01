package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to delete a story from the local repository.
     *
     * @param storyId The ID of the story to be deleted.
     * @return An integer representing the result of the deletion operation.
     */
    suspend operator fun invoke(storyId: String): Int {
        return repository.deleteStoryFromLocal(storyId)
    }
}