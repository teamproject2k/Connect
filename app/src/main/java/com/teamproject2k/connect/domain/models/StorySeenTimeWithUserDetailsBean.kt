package com.teamproject2k.connect.domain.models

data class StorySeenTimeWithUserDetailsBean(
    val seenBy: UserBean,
    val seenAt: Long
)