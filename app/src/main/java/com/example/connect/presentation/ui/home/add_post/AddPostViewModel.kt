package com.example.connect.presentation.ui.home.add_post

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.ui.models.PostVisibilityScope
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
@SuppressLint("StateNameRule")
class AddPostViewModel @Inject constructor() : BaseViewModel() {
    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<PostMediaData?> = mutableStateOf(null)
    lateinit var postVisibilityScopeList: List<PostVisibilityScope>
    lateinit var currentPostVisibilityState: MutableState<PostVisibilityScope>
    var isFirstTimeSetup = true
    val snackBarMessageState = mutableStateOf("")
    fun setUpData(context: Context) {
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        currentPostVisibilityState = mutableStateOf(postVisibilityScopeList[0])
        isFirstTimeSetup = false
    }
}