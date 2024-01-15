package com.example.connect.data.models.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.data.local_db.TableNames
import com.example.connect.domain.models.PostBean

@Entity(tableName = TableNames.POST_TABLE_NAME)
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
            likedBy,
            whetherDeleted
        )
    }
}