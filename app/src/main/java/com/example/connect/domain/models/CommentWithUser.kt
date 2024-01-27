package com.example.connect.domain.models

data class CommentWithUser(
    val comment: CommentBean,
    val userDetails: UsersBean,
    val commentedOnUserConnectId: String? = null
)