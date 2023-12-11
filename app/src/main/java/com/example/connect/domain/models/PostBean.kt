package com.example.connect.domain.models

import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.post.PostRemoteEntity


data class PostBean(
    var id: String,
    val fireBaseUserId: String,
    val postUrl: String,
    val caption: String,
    val createdAt: Long,
    val postType: String
) {
    fun toPostRemoteEntity(): PostRemoteEntity {
        return PostRemoteEntity(
            fireBaseUserId,
            postUrl,
            caption,
            createdAt,
            postType
        )
    }

    fun toPostDbEntity(): PostDbEntity {
        return PostDbEntity(
            id,
            fireBaseUserId,
            postUrl,
            caption,
            createdAt,
            postType
        )
    }
}