package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class AddAllStoriesToLocalUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend operator fun invoke(storiesList: List<StoryBean>): LongArray {
        return repository.addAllStoriesToLocal(storiesList)
    }
}