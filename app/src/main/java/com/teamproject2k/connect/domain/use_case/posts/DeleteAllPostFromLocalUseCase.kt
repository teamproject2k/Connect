package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(): Int {
        return repository.deleteAllPostFomLocal()
    }
}