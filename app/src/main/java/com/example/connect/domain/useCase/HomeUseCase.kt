package com.example.connect.domain.useCase

import android.net.Uri
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.domain.repository.IHomeRepository
import javax.inject.Inject

class HomeUseCase @Inject constructor(private val repository: IHomeRepository) {

    suspend fun getUserDetailsFromLocal(fireBaseId: String) =
        repository.getUserDetailsFromLocal(fireBaseId)

    suspend fun getUserDetailsFromServer(fireBaseId: String) =
        repository.getUserDetailsFromServer(fireBaseId)

    suspend fun addUserToLocalDb(userDetails: UserDetails) =
        repository.addUserToLocalDb(userDetails)

    suspend fun getPostDetailsFromLocal(fireBaseId: String) =
        repository.getPostDetailsFromLocal(fireBaseId)

    suspend fun getPostDetailsFromServer(fireBaseId: String) =
        repository.getPostDetailsFromServer(fireBaseId)

    suspend fun addPostToLocal(postDetails: PostDetails) =
        repository.addPostToLocal(postDetails)

    suspend fun addPostListToLocal(postDetailList: List<PostDetails>) =
        repository.addPostListToLocal(postDetailList)

    suspend fun getUserDetailsFromIds(idList: List<String>) =
        repository.getUserDetailsFromIds(idList)

    suspend fun updateUserDetailsOnServer(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ): ResponseState<Nothing?> {
        return repository.updateUserDetailsOnServer(fieldsToUpdate, firebaseUserId)
    }

    suspend fun getUsersFromName(name: Any) = repository.getUsersFromName(name)

    suspend fun updateImageOnRemoteStorage(
        imageUri: Uri?,
        firebaseUserId: String,
        parameterToUpdate: String
    ) = repository.updateImageOnRemoteStorage(imageUri, firebaseUserId, parameterToUpdate)

    suspend fun updateUserDetailsOnLocal(
        fieldsToUpdate: MutableMap<String, Any>,
        firebaseUserId: String
    ) = repository.updateUserDetailsOnLocal(fieldsToUpdate, firebaseUserId)


}