package com.example.connect.presentation.ui.home.settings_and_privacy

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.VisibilityScope
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class SettingsAndPrivacyViewModel @Inject constructor() : BaseViewModel() {

    lateinit var genderVisibilityScopeList: List<VisibilityScope>
    lateinit var dobVisibilityScopeList: List<VisibilityScope>
    lateinit var friendListVisibilityScopeList: List<VisibilityScope>
    lateinit var genderVisibilityState: MutableState<VisibilityScope>
    lateinit var dobVisibilityState: MutableState<VisibilityScope>
    lateinit var friendListVisibilityState: MutableState<VisibilityScope>

    val snackBarMessageState = mutableStateOf("")
    var isFirstTimeSetup = true

    fun setUpData(userDetails: UsersBean, context: Context) {

        val defaultGenderVisibility = if (userDetails.genderVisibility == "Public") 0 else 1
        val defaultDobVisibility = if (userDetails.dobVisibility == "Public") 0 else 1
        val defaultFriendListVisibility = if (userDetails.friendListVisibility == "Public") 0 else 1

        genderVisibilityScopeList = FunctionHelper.getGenderVisibilityList(context)
        dobVisibilityScopeList = FunctionHelper.getDobVisibilityList(context)
        friendListVisibilityScopeList = FunctionHelper.getFriendListVisibilityList(context)
        genderVisibilityState = mutableStateOf(genderVisibilityScopeList[defaultGenderVisibility])
        dobVisibilityState = mutableStateOf(genderVisibilityScopeList[defaultDobVisibility])
        friendListVisibilityState =
            mutableStateOf(genderVisibilityScopeList[defaultFriendListVisibility])
        isFirstTimeSetup = false
    }
}