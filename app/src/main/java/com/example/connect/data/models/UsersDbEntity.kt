package com.example.connect.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.utils.FunctionHelper

@Entity
data class UsersDbEntity(
    @PrimaryKey
    val firebaseUserId: String,
    val connectUserId: String,
    val name: String,
    val gender: String,
    val dateOfBirth: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    val bio: String,
    val profilePhoto: String? = null,
    val coverPhoto: String? = null,
    val friendList: List<String> = listOf()
) {
    fun toUserBean(): UsersBean {
        return UsersBean(
            firebaseUserId,
            connectUserId,
            FunctionHelper.getFormattedDisplayName(name),
            gender,
            dateOfBirth,
            createdAt,
            modifiedAt,
            currentLoggedInDeviceId,
            bio,
            profilePhoto,
            coverPhoto,
            friendList
        )
    }
}