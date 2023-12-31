package com.example.connect.domain.models

import com.example.connect.data.models.story.StoryDbEntity
import com.example.connect.data.models.story.StoryRemoteEntity
import java.io.Serializable

data class StoryBean(
    var id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val seenList: List<String>
) : Serializable {
    fun toStoryDbEntity(): StoryDbEntity {
        return StoryDbEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textOffset,
            backgroundGradientColor,
            seenList
        )
    }

    fun toStoryRemoteEntity(): StoryRemoteEntity {
        return StoryRemoteEntity(
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textOffset,
            backgroundGradientColor,
            seenList
        )
    }
}