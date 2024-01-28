package com.example.connect.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoriesWithUserBean(val usersBean: UsersBean, val storiesList: ArrayList<StoryBean>) :
    Parcelable