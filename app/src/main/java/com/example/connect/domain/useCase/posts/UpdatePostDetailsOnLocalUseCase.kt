package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UpdatePostDetailsOnLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(postDetails: PostBean): Int {
        return repository.updatePostDetailsOnLocal(postDetails)
    }
}