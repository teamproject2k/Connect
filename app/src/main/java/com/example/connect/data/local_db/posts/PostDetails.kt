package com.example.connect.data.local_db.posts

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class PostDetails(
    @PrimaryKey
    val id: String,
    val fireBaseUserId: String,
    val postUrl: String,
    val caption: String,
    val createdAt: Long,
    val postType: String,
    val thumbnailUrl: String = ""
)
