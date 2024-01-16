package com.example.connect.domain.useCase.posts

import com.example.connect.domain.models.CommentBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(
        comment: CommentBean
    ): ResponseState<String> {
        return repository.addCommentOnRemote(comment)
    }
}