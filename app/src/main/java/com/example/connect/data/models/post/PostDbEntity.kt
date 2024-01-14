package com.example.connect.data.models.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.PostBean

@Entity(tableName = "posts")
data class PostDbEntity(
    @PrimaryKey
    val postFirebaseId: String,
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postVisibilityScope: String,
    val postContentType: String,
    val commentCount: Long,
    val isSavedByCurrentUser: Boolean,
    val likedBy: ArrayList<String>,
    val whetherDeleted: Boolean
) {
    fun toPostBean(): PostBean {
        return PostBean(
            postFirebaseId,
            createdByUserFirebaseId,
            mediaUrl,
            caption,
            createdAt,
            postVisibilityScope,
            postContentType,
            commentCount,
            isSavedByCurrentUser,
            likedBy,
            whetherDeleted
        )
    }
}