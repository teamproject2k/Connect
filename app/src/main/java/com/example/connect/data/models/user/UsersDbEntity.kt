package com.example.connect.data.models.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.connect.common.VisibilityEnum
import com.example.connect.domain.enums.StatusWithCurrentEnum
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
    val otherUsersStatus: MutableMap<String, String> = mutableMapOf(),
    val genderVisibility: String = VisibilityEnum.Public.name,
    val dobVisibility: String = VisibilityEnum.Public.name,
    val friendListVisibility: String = VisibilityEnum.Public.name
) {
    fun toUserBean(): UsersBean {
        val friendList = mutableListOf<String>()
        val requestedFriendRequestList = mutableListOf<String>()
        val receivedFriendRequestList = mutableListOf<String>()
        val blockedUsersList = mutableListOf<String>()
        otherUsersStatus.forEach { entry ->
            when (entry.value) {
                StatusWithCurrentEnum.Friends.name -> {
                    friendList.add(entry.key)
                }

                StatusWithCurrentEnum.RequestedByCurrentUser.name -> {
                    requestedFriendRequestList.add(entry.key)
                }

                StatusWithCurrentEnum.RequestedByOtherUser.name -> {
                    receivedFriendRequestList.add(entry.key)
                }

                StatusWithCurrentEnum.Blocked.name -> {
                    blockedUsersList.add(entry.key)
                }
            }
        }
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
            friendList,
            requestedFriendRequestList,
            receivedFriendRequestList,
            blockedUsersList
        )
    }
}