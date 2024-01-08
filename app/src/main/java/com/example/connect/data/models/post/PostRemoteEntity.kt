package com.example.connect.data.models.post

import com.example.connect.domain.models.PostBean

data class PostRemoteEntity(
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String,
    val commentCount: Long,
    val likedBy: ArrayList<String>,
    val isDeleted: Boolean
) {
    constructor() : this("", "", "", 0, "", "", 0, arrayListOf(), false)

    fun toPostBean(id: String, isSavedByCurrentUser: Boolean): PostBean {
        return PostBean(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType,
            commentCount,
            isSavedByCurrentUser,
            likedBy,
            isDeleted
        )
    }
}