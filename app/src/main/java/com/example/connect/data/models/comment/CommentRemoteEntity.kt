package com.example.connect.data.models.comment

import com.example.connect.domain.models.CommentBean

data class CommentRemoteEntity(
    val createdAt: Long,
    val commentedBy: String,
    val commentedOn: String,
    val postId: String,
    val comment: String
) {
    constructor() : this(0L, "", "", "", "")

    fun toCommentBean(commentId: String): CommentBean {
        return CommentBean(
            commentId,
            createdAt,
            commentedBy,
            commentedOn,
            postId,
            comment
        )
    }
}