package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class UpdateStoryOnLocalUseCase @Inject constructor(private val repository: IStoryRepository) {

    suspend operator fun invoke(storyBean: StoryBean): Int {
        return repository.updateStoryOnLocal(storyBean)
    }
}