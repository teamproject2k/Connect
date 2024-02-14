package com.teamproject2k.connect.domain.models


data class ChatWithUserAndCountBean(
    val userDetails: UsersBean,
    val unreadMessageCount: Int,
    val lastMessage: ChatBean
)