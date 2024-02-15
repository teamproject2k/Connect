package com.teamproject2k.connect.data.models.story

import com.teamproject2k.connect.domain.models.StorySeenByBean


@Suppress("unused")

data class StorySeenByRemoteEntity(
    val seenUserId: String,
    val seenTime: Long
) {
    constructor() : this(seenUserId = "", seenTime = 0)


    fun toStorySeenByBean(): StorySeenByBean {
        return StorySeenByBean(seenUserId, seenTime)
    }
}