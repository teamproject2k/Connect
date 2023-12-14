package com.example.connect.data.models.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.PostBean

@Entity
data class PostDbEntity(
    @PrimaryKey
    val id: String,
    val fireBaseUserId: String,
    val mediaUrl: String,
    val caption: String,
    val createdAt: Long,
    val postScope: String,
    val postType: String
) {
    fun toPostBean(): PostBean {
        return PostBean(
            id,
            fireBaseUserId,
            mediaUrl,
            caption,
            createdAt,
            postScope,
            postType
        )
    }
}