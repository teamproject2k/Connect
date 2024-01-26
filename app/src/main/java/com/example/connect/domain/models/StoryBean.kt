package com.example.connect.domain.models

import android.os.Parcelable
import com.example.connect.data.models.story.StoryDbEntity
import com.example.connect.data.models.story.StoryRemoteEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class StoryBean(
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
    val whetherDeleted: Boolean = false
) : Parcelable {
    fun toStoryRemoteEntity(): StoryRemoteEntity {
        return StoryRemoteEntity(
            createdByUserFirebaseId,
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

    fun toStoryDbEntity(): StoryDbEntity {
        return StoryDbEntity(
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
            whetherDeleted
        )
    }
}