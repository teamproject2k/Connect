package com.example.connect.data.models.post

import com.example.connect.domain.models.PostBean

data class PostRemoteEntity(
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String
) {
    fun toPostBean(id: String): PostBean {
        return PostBean(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType
        )
    }
}