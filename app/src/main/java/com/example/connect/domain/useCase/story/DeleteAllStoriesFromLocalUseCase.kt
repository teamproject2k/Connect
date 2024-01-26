package com.example.connect.domain.useCase.story

import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteAllStoriesFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {

    suspend operator fun invoke(): Int {
        return repository.deleteAllStoriesFromLocal()
    }
}