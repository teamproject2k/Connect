package com.example.connect.presentation.ui.home.add_story

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.MediaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AddStoryViewModel @Inject constructor() : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _uploadStoryStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadStoryStateFlow: StateFlow<ResponseState<Nothing>> get() = _uploadStoryStateFlow

    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)

    var defaultStoryBackgroundColorState: MutableState<Color> = mutableStateOf(Color.Black)
}