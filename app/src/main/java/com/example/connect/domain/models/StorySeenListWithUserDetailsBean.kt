package com.example.connect.domain.models

data class StorySeenListWithUserDetailsBean(
    val seenBy: UsersBean,
    val seenAt: Long
)