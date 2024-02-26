package com.teamproject2k.connect.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoriesWithUserBean(val userBean: UserBean, val storiesList: ArrayList<StoryBean>) :
    Parcelable