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

    suspend fun updateUserDetails(fieldsToUpdate: MutableMap<String, String>): ResponseState<String> {
        return repository.updateUserDetails(fieldsToUpdate)
    }

    suspend fun getUsersFromName(name: String) = repository.getUsersFromName(name)

    suspend fun updateProfileImageOnRemoteStorage(profileImage: Uri?, firebaseUserId: String) =
        repository.updateProfileImageOnRemoteStorage(profileImage, firebaseUserId)

    suspend fun updateCoverImageOnRemoteStorage(coverImage: Uri?, firebaseUserId: String) =
        repository.updateCoverImageOnRemoteStorage(coverImage, firebaseUserId)

}