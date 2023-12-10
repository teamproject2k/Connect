package com.example.connect.domain.models

import com.example.connect.data.models.UserRemoteEntity
import com.example.connect.data.models.UsersDbEntity

data class UsersBean(
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
    fun toUserRemoteEntity(): UserRemoteEntity {
        return UserRemoteEntity(
            firebaseUserId,
            connectUserId,
            name,
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

    fun toUserDbEntity(): UsersDbEntity {
        return UsersDbEntity(
            firebaseUserId,
            connectUserId,
            name,
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