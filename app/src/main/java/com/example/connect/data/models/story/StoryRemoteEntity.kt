package com.example.connect.data.models.story

import com.example.connect.domain.models.StoryBean

data class StoryRemoteEntity(
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val seenList: List<String>
) {
    constructor() : this("", "", "", 0, "", "", "", emptyList())

    fun toStoryBean(storyId: String): StoryBean {
        return StoryBean(
            storyId,
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