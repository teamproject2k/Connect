package com.teamproject2k.connect.data.models.story

import com.teamproject2k.connect.domain.models.StorySeenByBean

data class StorySeenByLocalEntity(
    val seenUserId: String,
    val seenTime: Long
) {
    fun toStorySeenByBean(): StorySeenByBean {
        return StorySeenByBean(seenUserId, seenTime)
    }
}