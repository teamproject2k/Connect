package com.example.connect.domain.models

import com.example.connect.data.models.story.StoryDbEntity
import com.example.connect.data.models.story.StoryRemoteEntity

data class StoryBean(
    val id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String
) {
    fun toStoryDbEntity(): StoryDbEntity {
        return StoryDbEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType
        )
    }

    fun toStoryRemoteEntity(): StoryRemoteEntity {
        return StoryRemoteEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType
        )
    }
}