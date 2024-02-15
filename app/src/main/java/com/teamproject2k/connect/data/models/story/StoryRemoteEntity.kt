package com.teamproject2k.connect.data.models.story

import com.teamproject2k.connect.domain.models.StoryBean

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
    constructor() : this(
        createdByUserFirebaseId = "",
        mediaUrl = "",
        caption = "",
        createdAt = 0,
        mediaType = "",
        textColor = "",
        textOffset = "",
        backgroundGradientColor = "",
        seenBy = arrayListOf(),
        videoLength = 0,
        whetherDeleted = false
    )

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