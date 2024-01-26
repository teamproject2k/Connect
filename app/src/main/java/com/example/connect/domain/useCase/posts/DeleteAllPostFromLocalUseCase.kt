package com.example.connect.domain.useCase.posts

import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(): Int {
        return repository.deleteAllPostFomLocal()
    }
}