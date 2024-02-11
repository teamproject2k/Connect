package com.example.connect.presentation.ui.chat.add_media

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.MediaData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddMediaViewModel @Inject constructor() : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)


}