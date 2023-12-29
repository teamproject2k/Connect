package com.example.connect.data.models.story

import com.example.connect.domain.models.StoryBean

data class StoryRemoteEntity(
    val id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String
) {
    fun toStoryBean(): StoryBean {
        return StoryBean(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            mediaType
        )
    }
}