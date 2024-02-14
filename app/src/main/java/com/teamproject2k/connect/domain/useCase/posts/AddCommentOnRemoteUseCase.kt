package com.teamproject2k.connect.domain.useCase.posts

import com.teamproject2k.connect.domain.models.CommentBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(
        comment: CommentBean
    ): ResponseState<String> {
        return repository.addCommentOnRemote(comment)
    }
}