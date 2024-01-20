package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddLikeOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.addLikeOnPostOnRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}