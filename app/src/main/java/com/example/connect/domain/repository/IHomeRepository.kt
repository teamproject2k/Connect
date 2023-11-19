package com.example.connect.domain.repository

import android.net.Uri
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails

interface IHomeRepository {
    suspend fun getUserDetailsFromLocal(fireBaseId: String): UserDetails?

    suspend fun getUserDetailsFromServer(fireBaseId: String): ResponseState<UserDetails>

    suspend fun addUserToLocalDb(userDetails: UserDetails): Long

    suspend fun getUserDetailsFromIds(idList: List<String>): ResponseState<List<UserDetails>>

    suspend fun getPostDetailsFromLocal(fireBaseId: String): List<PostDetails>?

    suspend fun getPostDetailsFromServer(fireBaseId: String): ResponseState<List<PostDetails>>

    suspend fun addPostToLocal(postDetails: PostDetails): Long

    suspend fun addPostListToLocal(postDetailList: List<PostDetails>): LongArray

    suspend fun updateUserDetails(fieldsToUpdate: MutableMap<String, String>): ResponseState<String>

    suspend fun getUsersFromName(name: String): ResponseState<Int>

    suspend fun updateProfileImageOnRemoteStorage(profileImage: Uri?, firebaseUserId: String): ResponseState<String>

    suspend fun updateCoverImageOnRemoteStorage(coverImage: Uri?, firebaseUserId: String): ResponseState<String>

}