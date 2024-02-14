package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class AddLikeForCommentOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend operator fun invoke(commentId: String, loggedInUserFirebaseId: String): ResponseState<Nothing> {
        return repository.addLikeForCommentOnRemote(commentId, loggedInUserFirebaseId)
    }
}