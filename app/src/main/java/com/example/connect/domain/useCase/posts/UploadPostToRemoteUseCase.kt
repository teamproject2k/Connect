package com.example.connect.domain.useCase.posts

import com.example.connect.common.ResponseState
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class UploadPostToRemoteUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Invokes the upload post to server use case.
     *
     * @param postDetails The post details.
     * @param fireBaseId The fire base id.
     * @return The response state.
     */
    suspend fun invoke(postDetails: PostBean, fireBaseId: String): ResponseState<String> {
        return repository.uploadPostToRemote(postDetails, fireBaseId)
    }
}