package com.example.connect.presentation.ui.models

data class UserDetails(
    val userId: String,
    val name: String,
    val gender: String,
    val dateOfBirth: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val currentLoggedInDeviceId: String,
    val profilePhoto: String? = null,
    val bio: String? = null,
)