package com.example.connect.data.models.story

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.StoryBean

@Entity
data class StoryDbEntity(
    @PrimaryKey
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