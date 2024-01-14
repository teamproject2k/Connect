package com.example.connect.data.models.post

import androidx.room.Embedded
import androidx.room.Relation
import com.example.connect.data.models.user.UsersDbEntity

data class PostWithUserDetailsFromLocal(
    @Embedded
    val postDetail: PostDbEntity,

    @Relation(
        parentColumn = "createdByUserFirebaseId",
        entityColumn = "firebaseUserId"
    )
    val userDetail: UsersDbEntity?
)