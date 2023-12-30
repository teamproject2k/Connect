package com.example.connect.presentation.ui.home.show_story

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShowStoryViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentStoryState = mutableIntStateOf(0)

}