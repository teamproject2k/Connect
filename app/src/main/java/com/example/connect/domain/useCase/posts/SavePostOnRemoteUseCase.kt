package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class SavePostOnRemoteUseCase @Inject constructor(private val repository: IUserRepository) {

    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.savePostOnRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}