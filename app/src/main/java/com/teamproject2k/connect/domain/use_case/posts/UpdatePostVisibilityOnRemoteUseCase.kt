package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UpdatePostVisibilityOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(postFirebaseId: String, postScopeName: String): ResponseState<Nothing> {
        return repository.updatePostVisibilityOnRemote(postFirebaseId, postScopeName)
    }
}