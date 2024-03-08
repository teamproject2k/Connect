package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteAllStoriesFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    /**
     * Suspended function to delete all stories from the local repository.
     *
     * @return An integer representing the number of stories deleted from the local repository.
     */
    suspend operator fun invoke(): Int {
        return repository.deleteAllStoriesFromLocal()
    }
}