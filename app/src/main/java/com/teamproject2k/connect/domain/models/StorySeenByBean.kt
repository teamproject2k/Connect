package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import com.teamproject2k.connect.data.models.story.StorySeenByLocalEntity
import com.teamproject2k.connect.data.models.story.StorySeenByRemoteEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class StorySeenByBean(
    val seenUserId: String,
    val seenTime: Long
) : Parcelable {
    fun toStoryRemoteEntity(): StorySeenByRemoteEntity {
        return StorySeenByRemoteEntity(seenUserId, seenTime)
    }

    fun toStoryLocalEntity(): StorySeenByLocalEntity {
        return StorySeenByLocalEntity(seenUserId, seenTime)
    }
}