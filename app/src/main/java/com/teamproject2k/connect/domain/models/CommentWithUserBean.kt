package com.teamproject2k.connect.domain.models

data class CommentWithUserBean(
    val comment: CommentBean,
    val userDetails: UserBean,
    val commentedOnUserConnectId: String? = null
)