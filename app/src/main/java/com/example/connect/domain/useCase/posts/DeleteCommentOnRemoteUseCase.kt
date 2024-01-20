package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(
        commentId: String,
        postFirebaseId: String,
        deleteCount: Int,
    ): ResponseState<String> {
        return repository.deleteCommentOnRemote(commentId, postFirebaseId, deleteCount)
    }
}