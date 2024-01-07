package com.example.connect.data.models.comment

import com.example.connect.domain.models.CommentBean

data class CommentRemoteEntity(
    val createdAt: Long,
    val commentedBy: String,
    val parentCommentId: String?,
    val repliedOnCommentId: String?,
    val repliedOnUserId: String?,
    val postId: String,
    val comment: String,
    val whetherDeleted: Boolean
) {
    constructor() : this(0L, "", "", "", "", "", "", false)

    fun toCommentBean(commentId: String): CommentBean {
        return CommentBean(
            commentId,
            createdAt,
            commentedBy,
            parentCommentId,
            repliedOnCommentId,
            repliedOnUserId,
            postId,
            comment,
            whetherDeleted
        )
    }
}