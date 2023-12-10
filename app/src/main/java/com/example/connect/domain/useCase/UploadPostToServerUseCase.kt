package com.example.connect.domain.useCase

import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class UploadPostToServerUseCase @Inject constructor(private val repository: IHomeRepository) {

    /**
     * Uploads a post to the server.
     *
     * @param postDetails The details of the post to upload.
     * @param fireBaseId The Firebase ID of the user uploading the post.
     */
    suspend fun uploadPostToServer(postDetails: PostDetails, fireBaseId: String) {
        repository.uploadPostToServer(postDetails, fireBaseId)
    }
}