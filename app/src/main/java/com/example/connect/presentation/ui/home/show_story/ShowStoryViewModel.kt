package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class ShowStoryViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentStoryState = mutableIntStateOf(0)
    lateinit var currentStoryPosterState: MutableState<UsersBean>
    var isCurrentStoryPosterInitialized = false
}