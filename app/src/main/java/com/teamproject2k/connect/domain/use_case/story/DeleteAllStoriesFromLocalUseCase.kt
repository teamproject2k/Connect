package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class DeleteAllStoriesFromLocalUseCase @Inject constructor(private val repository: IStoryRepository) {

    suspend operator fun invoke(): Int {
        return repository.deleteAllStoriesFromLocal()
    }
}