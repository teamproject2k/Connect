package com.example.connect.data.models.comment

import com.example.connect.domain.models.CommentBean

data class CommentRemoteEntity(
    val commentedTime: Long,
    val commentedBy: String,
    val commentedOn: String,
    val postId: String,
    val comment: String
) {
    constructor() : this(0L, "", "", "", "")

    fun toCommentBean(commentId: String): CommentBean {
        return CommentBean(
            commentId,
            commentedTime,
            commentedBy,
            commentedOn,
            postId,
            comment
        )
    }
}