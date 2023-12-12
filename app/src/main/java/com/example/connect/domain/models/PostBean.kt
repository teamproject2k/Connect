package com.example.connect.domain.models

import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.post.PostRemoteEntity


data class PostBean(
    var id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String
) {
    fun toPostRemoteEntity(): PostRemoteEntity {
        return PostRemoteEntity(
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType
        )
    }

    fun toPostDbEntity(): PostDbEntity {
        return PostDbEntity(
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