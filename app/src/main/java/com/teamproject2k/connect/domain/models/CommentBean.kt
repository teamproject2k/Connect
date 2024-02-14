package com.teamproject2k.connect.domain.models

import com.teamproject2k.connect.data.models.comment.CommentRemoteEntity

data class CommentBean(
    var commentFirebaseId: String,
    val createdAt: Long,
    val commentedBy: String,
    val parentCommentId: String?,
    val repliedOnCommentId: String?,
    val repliedOnUserId: String?,
    val postFirebaseId: String,
    val commentMessage: String,
    var whetherDeleted: Boolean,
    val likedBy: ArrayList<String>
) {
    fun toCommentRemoteEntity(): CommentRemoteEntity {
        return CommentRemoteEntity(
            createdAt,
            commentedBy,
            parentCommentId,
            repliedOnCommentId,
            repliedOnUserId,
            postFirebaseId,
            commentMessage,
            whetherDeleted,
            likedBy
        )
    }
}