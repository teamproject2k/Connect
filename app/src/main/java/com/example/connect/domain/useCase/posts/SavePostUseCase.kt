package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class SavePostUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend fun invoke(loggedInUserFirebaseId: String, postId: String): ResponseState<Nothing> {
        return repository.savePost(loggedInUserFirebaseId, postId)
    }
}