package com.example.connect.presentation.ui.chat.show_media

import androidx.compose.runtime.mutableStateOf
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShowMediaViewModel @Inject constructor() : BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")

}