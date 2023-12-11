package com.example.connect.data.models.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.PostBean

@Entity
data class PostDbEntity(
    @PrimaryKey
    val id: String,
    val fireBaseUserId: String,
    val postUrl: String,
    val caption: String,
    val createdAt: Long,
    val postType: String,
) {
    fun toPostBean(): PostBean {
        return PostBean(
            id,
            fireBaseUserId,
            postUrl,
            caption,
            createdAt,
            postType
        )
    }
}