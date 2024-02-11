package com.example.connect.data.models.story

import com.example.connect.domain.models.StoryBean

data class StoryRemoteEntity(
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val mediaType: String,
    val textColor: String,
    val textOffset: String,
    val backgroundGradientColor: String,
    val seenBy: ArrayList<StorySeenByRemoteEntity>,
    val videoLength: Long = 0,
    val whetherDeleted: Boolean = false
) {
    constructor() : this("", "", "", 0, "", "", "", "", arrayListOf(), 0, false)

    fun toStoryBean(storyId: String): StoryBean {
        return StoryBean(
            storyId,
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