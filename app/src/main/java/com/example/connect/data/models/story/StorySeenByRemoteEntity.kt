package com.example.connect.data.models.story

data class StorySeenByRemoteEntity(
    val seenUserId: String,
    val seenTime: Long
) {
    constructor() : this("", 0)
}