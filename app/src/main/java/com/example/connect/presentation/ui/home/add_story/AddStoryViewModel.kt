package com.example.connect.presentation.ui.home.add_story

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.FunctionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SuppressLint("StateNameRule")

class AddStoryViewModel @Inject constructor() : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _uploadStoryStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadStoryStateFlow: StateFlow<ResponseState<Nothing>> get() = _uploadStoryStateFlow

    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)

    var gradientColorList: List<List<Color>> = FunctionHelper.getStoryBackgroundColorList()
    var defaultStoryBackgroundColorState: MutableState<List<Color>> =
        mutableStateOf(gradientColorList[0])

}