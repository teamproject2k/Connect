package com.example.connect.domain.models

data class CommentWithUserBean(
    val comment: CommentBean,
    val userDetails: UsersBean,
    val commentedOnUserConnectId: String? = null
)