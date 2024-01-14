package com.example.connect.domain.useCase.posts

import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IPostRepository
import com.example.connect.presentation.ui.models.VisibilityScope
import javax.inject.Inject

class UpdatePostVisibilityOnRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(postFirebaseId: String, postScopeName: String): ResponseState<Nothing> {
        return repository.updatePostVisibilityOnRemote(postFirebaseId, postScopeName)
    }
}