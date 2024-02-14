package com.teamproject2k.connect.domain.useCase.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class UpdateStoryOnLocalUseCase @Inject constructor(private val repository: IStoryRepository) {

    suspend operator fun invoke(storyBean: StoryBean): Int {
        return repository.updateStoryOnLocal(storyBean)
    }
}