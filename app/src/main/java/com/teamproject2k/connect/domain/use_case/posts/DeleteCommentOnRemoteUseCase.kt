package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(
        commentId: String,
        postFirebaseId: String,
        deleteCount: Int,
    ): ResponseState<String> {
        return repository.deleteCommentOnRemote(commentId, postFirebaseId, deleteCount)
    }
}