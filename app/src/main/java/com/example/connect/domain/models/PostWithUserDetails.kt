package com.example.connect.domain.models

data class PostWithUserDetails(
    val postDetail: PostBean,
    val userDetail: UsersBean
)