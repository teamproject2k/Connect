package com.example.connect.data.models.story

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.StoryBean

@Entity(tableName = "stories")
data class StoryLocalEntity(
    @PrimaryKey
    var storyFirebaseId: String,
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textColor: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val videoLength: Long = 0,
    val seenBy: List<StorySeenByLocalEntity>,
    val whetherDeleted: Boolean = false
) {
    fun toStoryBean(): StoryBean {
        return StoryBean(
            storyFirebaseId,
            createdByUserFirebaseId,
            mediaUrl,
            caption,
            createdAt,
            mediaType,
            textColor,
            textOffset,
            backgroundGradientColor,
            videoLength,
            seenBy.map { it.toStorySeenByBean() } as ArrayList,
            whetherDeleted
        )
    }
}