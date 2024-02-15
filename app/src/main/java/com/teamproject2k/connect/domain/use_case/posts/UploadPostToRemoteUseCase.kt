package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UploadPostToRemoteUseCase @Inject constructor(private val repository: IPostRepository) {
    /**
     * Invokes the upload post to server use case.
     *
     * @param postDetails The post details.
     * @param fireBaseId The fire base id.
     * @return The response state.
     */
    suspend operator fun invoke(postDetails: PostBean, fireBaseId: String): ResponseState<String> {
        return repository.uploadPostToRemote(postDetails, fireBaseId)
    }
}