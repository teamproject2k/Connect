package com.teamproject2k.connect.data.models.comment

import com.teamproject2k.connect.domain.models.CommentBean

data class CommentRemoteEntity(
    val createdAt: Long,
    val commentedBy: String,
    val parentCommentId: String?,
    val repliedOnCommentId: String?,
    val repliedOnUserId: String?,
    val postFirebaseId: String,
    val commentMessage: String,
    val whetherDeleted: Boolean,
    val likedBy: ArrayList<String>
) {
    constructor() : this(
        createdAt = 0L,
        commentedBy = "",
        parentCommentId = "",
        repliedOnCommentId = "",
        repliedOnUserId = "",
        postFirebaseId = "",
        commentMessage = "",
        whetherDeleted = false,
        likedBy = arrayListOf()
    )

    fun toCommentBean(commentId: String): CommentBean {
        return CommentBean(
            commentId,
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