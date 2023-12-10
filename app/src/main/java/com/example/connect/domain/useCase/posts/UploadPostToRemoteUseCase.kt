package com.example.connect.domain.useCase.posts

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
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
    suspend fun invoke(postDetails: PostDetails, fireBaseId: String): ResponseState<String> {
        return repository.uploadPostToRemote(postDetails, fireBaseId)
    }
}