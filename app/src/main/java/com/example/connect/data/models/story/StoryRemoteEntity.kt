package com.example.connect.data.models.story

import com.example.connect.domain.models.StoryBean

data class StoryRemoteEntity(
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textColor: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val videoLength: Long = 0,
    val whetherDeleted: Boolean = false
) {
    constructor() : this("", "", "", 0, "", "", "", "", 0, false)

    fun toStoryBean(storyId: String): StoryBean {
        return StoryBean(
            storyId,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textColor,
            textOffset,
            backgroundGradientColor,
            videoLength,
            whetherDeleted
        )
    }
}