package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddAllStoriesToLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend fun invoke(storiesList: List<StoryBean>): LongArray {
        return repository.addAllStoriesToLocal(storiesList)
    }
}