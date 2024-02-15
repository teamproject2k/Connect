package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import com.teamproject2k.connect.data.models.post.PostLocalEntity
import com.teamproject2k.connect.data.models.post.PostRemoteEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostBean(
    var postFirebaseId: String,
    val createdByUserFirebaseId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    var postVisibilityScope: String,
    val postContentType: String,
    var commentCount: Long,
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

    fun toPostLocalEntity(): PostLocalEntity {
        return PostLocalEntity(
            postFirebaseId,
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
}