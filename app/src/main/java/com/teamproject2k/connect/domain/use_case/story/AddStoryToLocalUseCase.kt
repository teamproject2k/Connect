package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddStoryToLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storyDetails: StoryBean): Long {
        return repository.addStoryToLocal(storyDetails)
    }
}