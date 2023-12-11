package com.example.connect.data.models.post

import com.example.connect.domain.models.PostBean

data class PostRemoteEntity(
    val fireBaseUserId: String,
    val postUrl: String,
    val caption: String,
    val createdAt: Long,
    val postType: String,
) {
    fun toPostBean(id: String): PostBean {
        return PostBean(
            id,
            fireBaseUserId,
            postUrl,
            caption,
            createdAt,
            postType
        )
    }
}