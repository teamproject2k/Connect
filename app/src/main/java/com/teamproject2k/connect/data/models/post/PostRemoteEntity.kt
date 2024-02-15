package com.teamproject2k.connect.data.models.post

import com.teamproject2k.connect.domain.models.PostBean

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
    constructor() : this(
        createdByUserFirebaseId = "",
        mediaUrl = "",
        caption = "",
        createdAt = 0,
        postVisibilityScope = "",
        postContentType = "",
        commentCount = 0,
        likedBy = arrayListOf(),
        whetherDeleted = false
    )

    fun toPostBean(id: String): PostBean {
        return PostBean(
            id,
            createdByUserFirebaseId,
            mediaUrl,
            caption,
            createdAt,
            postVisibilityScope,
            postContentType,
            commentCount,
            likedBy,
            whetherDeleted
        )
    }
}