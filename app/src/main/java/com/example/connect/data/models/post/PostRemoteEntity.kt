package com.example.connect.data.models.post

import com.example.connect.domain.models.PostBean

data class PostRemoteEntity(
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postVisibilityScope: String,
    val postContentType: String,
    val commentCount: Long,
    val likedBy: ArrayList<String>,
    val whetherDeleted: Boolean
) {
    constructor() : this("", "", "", 0, "", "", 0, arrayListOf(), false)

    fun toPostBean(id: String, isSavedByCurrentUser: Boolean): PostBean {
        return PostBean(
            id,
            createdByUserFirebaseId,
            mediaUrl,
            caption,
            createdAt,
            postVisibilityScope,
            postContentType,
            commentCount,
            isSavedByCurrentUser,
            likedBy,
            whetherDeleted
        )
    }
}