package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class RemoveLikeForCommentFromRemoteUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend fun invoke(commentId: String, loggedInUserFirebaseId: String): ResponseState<Nothing> {
        return repository.removeLikeForCommentFromRemote(commentId, loggedInUserFirebaseId)
    }
}