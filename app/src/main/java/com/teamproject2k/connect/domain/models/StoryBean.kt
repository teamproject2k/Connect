package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import com.teamproject2k.connect.data.models.story.StoryLocalEntity
import com.teamproject2k.connect.data.models.story.StoryRemoteEntity
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
    val seenBy: ArrayList<StorySeenByBean>,
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
            seenBy.map { it.toStoryRemoteEntity() } as ArrayList,
            videoLength,
            whetherDeleted
        )
    }

    fun toStoryLocalEntity(): StoryLocalEntity {
        return StoryLocalEntity(
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
            seenBy.map { it.toStoryLocalEntity() } as ArrayList,
            whetherDeleted
        )
    }
}