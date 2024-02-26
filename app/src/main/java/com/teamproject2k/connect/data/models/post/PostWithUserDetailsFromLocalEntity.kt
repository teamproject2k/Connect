package com.teamproject2k.connect.data.models.post

import androidx.room.Embedded
import androidx.room.Relation
import com.teamproject2k.connect.data.models.user.UsersLocalEntity

data class PostWithUserDetailsFromLocalEntity(
    @Embedded
    val postDetail: PostLocalEntity,

    @Relation(
        parentColumn = "createdByUserFirebaseId",
        entityColumn = "firebaseUserId"
    )
    val userDetail: UsersLocalEntity?
)