package com.teamproject2k.connect.domain.useCase.story

import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyId: String): Int {
        return repository.deleteStoryFromLocal(storyId)
    }
}