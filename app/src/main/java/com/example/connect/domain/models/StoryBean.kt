package com.example.connect.domain.models

import com.example.connect.data.models.story.StoryRemoteEntity
import java.io.Serializable

data class StoryBean(
    var id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textColor: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val videoLength: Long = 0,
    val isDeleted: Boolean = false
) : Serializable {
    fun toStoryRemoteEntity(): StoryRemoteEntity {
        return StoryRemoteEntity(
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textColor,
            textOffset,
            backgroundGradientColor,
            videoLength,
            isDeleted
        )
    }
}