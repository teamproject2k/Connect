package com.example.connect.domain.useCase.posts

import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeletePostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(postFirebaseId: String) {
        repository.deletePostFromLocal(postFirebaseId)
    }
}