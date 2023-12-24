package com.example.connect.domain.models

import android.os.Parcelable
import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.post.PostRemoteEntity
import kotlinx.parcelize.Parcelize


@Parcelize
data class PostBean(
    var id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String,
    val commentCount: Long,
    var isSavedByCurrentUser: Boolean,
    val likedBy: ArrayList<String>,
) : Parcelable {
    fun toPostRemoteEntity(): PostRemoteEntity {
        return PostRemoteEntity(
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType,
            commentCount,
            likedBy
        )
    }

    fun toPostDbEntity(): PostDbEntity {
        return PostDbEntity(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType,
            commentCount,
            isSavedByCurrentUser,
            likedBy
        )
    }
}