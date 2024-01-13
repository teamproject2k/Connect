package com.example.connect.domain.models

import android.os.Parcelable
import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.post.PostRemoteEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class PostBean(
    var postFirebaseId: String,
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postVisibilityScope: String,
    val postContentType: String,
    var commentCount: Long,
    var isSavedByCurrentUser: Boolean,
    val likedBy: ArrayList<String>,
    var whetherDeleted: Boolean
) : Parcelable {
    fun toPostRemoteEntity(): PostRemoteEntity {
        return PostRemoteEntity(
            createdByUserFirebaseId,
            mediaUrl,
            caption,
            createdAt,
            postVisibilityScope,
            postContentType,
            commentCount,
            likedBy,
            whetherDeleted
        )
    }

    fun toPostDbEntity(): PostDbEntity {
        return PostDbEntity(
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