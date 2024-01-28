package com.example.connect.domain.useCase.story

import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteStoryFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyId: String): Int {
        return repository.deleteStoryFromLocal(storyId)
    }
}