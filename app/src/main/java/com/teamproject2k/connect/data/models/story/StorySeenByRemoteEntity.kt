package com.teamproject2k.connect.data.models.story

import com.teamproject2k.connect.domain.models.StorySeenByBean

data class StorySeenByRemoteEntity(
    val seenUserId: String,
    val seenTime: Long
) {
    constructor() : this("", 0)


    fun toStorySeenByBean(): StorySeenByBean {
        return StorySeenByBean(seenUserId, seenTime)
    }
}