package com.example.connect.data.local_db.users

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class UserDetails(
    @PrimaryKey
    val firebaseUserId: String,
    val connectUserId: String,
    val name: String,
    val gender: String,
    val dateOfBirth: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    val bio: String = "Connect User",
    val profilePhoto: String? = null,
) : Serializable {
    constructor() : this("1", "", "", "", -1, 0, 0, "")
}