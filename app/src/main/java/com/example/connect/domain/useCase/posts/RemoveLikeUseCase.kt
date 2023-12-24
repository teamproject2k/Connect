package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class RemoveLikeUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(
        currentUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.removeLikeOf(currentUserFirebaseId, postFirebaseId)
    }
}