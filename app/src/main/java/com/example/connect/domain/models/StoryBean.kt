package com.example.connect.domain.models

import com.example.connect.data.models.story.StoryDbEntity
import com.example.connect.data.models.story.StoryRemoteEntity

data class StoryBean(
    var id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textOffset: String,
    val backgroundGradientColor: String
) {
    fun toStoryDbEntity(): StoryDbEntity {
        return StoryDbEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textOffset,
            backgroundGradientColor
        )
    }

    fun toStoryRemoteEntity(): StoryRemoteEntity {
        return StoryRemoteEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textOffset,
            backgroundGradientColor
        )
    }
}