package com.example.connect.data.models.story

import com.example.connect.domain.models.StorySeenByBean

data class StorySeenByLocalEntity(
    val seenUserId: String,
    val seenTime: Long
) {
    fun toStorySeenByBean(): StorySeenByBean {
        return StorySeenByBean(seenUserId, seenTime)
    }
}