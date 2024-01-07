package com.example.connect.domain.models

import com.example.connect.data.models.comment.CommentRemoteEntity

data class CommentBean(
    var commentFirebaseId: String,
    val createdAt: Long,
    val commentedBy: String,
    val parentCommentId: String?,
    val repliedOnCommentId: String?,
    val repliedOnUserId: String?,
    val postId: String,
    val comment: String
) {
    fun toCommentRemoteEntity(): CommentRemoteEntity {
        return CommentRemoteEntity(
            createdAt,
            commentedBy,
            parentCommentId,
            repliedOnCommentId,
            repliedOnUserId,
            postId,
            comment
        )
    }
}