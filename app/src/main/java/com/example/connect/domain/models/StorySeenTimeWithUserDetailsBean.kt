package com.example.connect.domain.models

data class StorySeenTimeWithUserDetailsBean(
    val seenBy: UsersBean,
    val seenAt: Long
)