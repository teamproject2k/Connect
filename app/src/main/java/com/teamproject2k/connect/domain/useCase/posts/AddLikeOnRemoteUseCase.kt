package com.teamproject2k.connect.domain.useCase.posts

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddLikeOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend operator fun invoke(
        loggedInUserFirebaseId: String,
        postFirebaseId: String
    ): ResponseState<Nothing> {
        return repository.addLikeOnPostOnRemote(loggedInUserFirebaseId, postFirebaseId)
    }
}