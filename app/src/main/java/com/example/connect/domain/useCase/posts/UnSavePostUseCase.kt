package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UnSavePostUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(currentUserFirebaseId: String, postId: String): ResponseState<Nothing> {
        return repository.unSavePost(currentUserFirebaseId, postId)
    }
}