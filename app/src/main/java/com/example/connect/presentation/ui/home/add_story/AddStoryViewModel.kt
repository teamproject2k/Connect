package com.example.connect.presentation.ui.home.add_story

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.story.AddStoryToRemoteUseCase
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val addStoryToRemoteUseCase: AddStoryToRemoteUseCase,
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _uploadStoryStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadStoryStateFlow: StateFlow<ResponseState<Nothing>> get() = _uploadStoryStateFlow

    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)

    var gradientColorList: List<List<Color>> = FunctionHelper.getStoryBackgroundColorList()
    var storyBackgroundColorState: MutableState<List<Color>> =
        mutableStateOf(gradientColorList[0])

    var textColorList: List<Color> = FunctionHelper.getStoryTextColorList()

    var captionOffsetX by mutableFloatStateOf(0f)
    var captionOffsetY by mutableFloatStateOf(0f)

    lateinit var colorOnMedia: MutableState<Color>

    var isDataInitialized = false

    var isFirstTimePlaced = true


    fun initData(textColor: Color) {
        this.colorOnMedia = mutableStateOf(textColor)
        isDataInitialized = true
    }

    fun uploadUserStory(loggedInUserFirebaseId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the upload operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the upload story state to loading.
                _uploadStoryStateFlow.value = ResponseState.loading()
                // Initialize the file URL to an empty string.
                var fileUrl = ""
                // Check if the user has selected any media.
                if (selectedMediaState.value != null) {
                    // Upload the selected media to the remote server.
                    val uploadFileToRemoteResponse =
                        uploadFileToRemoteUseCase.invoke(
                            selectedMediaState.value!!.uri,
                            "${FirebaseConstants.STORY_KEY}/$loggedInUserFirebaseId/${System.currentTimeMillis()}"
                        )

                    // Check if the upload operation was successful.
                    if (uploadFileToRemoteResponse.status == RequestStatusEnum.Exception) {
                        // Set the upload story state to error.
                        _uploadStoryStateFlow.value =
                            ResponseState.error(uploadFileToRemoteResponse.message ?: "")
                        return@withContext
                    } else {
                        // Get the file URL from the response.
                        fileUrl = uploadFileToRemoteResponse.data ?: ""
                    }
                }

                // Determine the story type based on the selected media and caption text.
                val storyType =
                    when {
                        // If no media is selected, the story type is Text.
                        selectedMediaState.value == null -> {
                            MediaTypeEnum.Text.name
                        }

                        // If the selected media is an image, the story type is Image or TextImage.
                        selectedMediaState.value!!.mediaType.contains(ConstantsHelper.MEDIA_TYPE_IMAGE) -> {
                            if (captionTextState.value.isNotBlank()) {
                                MediaTypeEnum.TextImage.name
                            } else {
                                MediaTypeEnum.Image.name
                            }
                        }

                        // If the selected media is a video, the story type is Video or TextVideo.
                        selectedMediaState.value!!.mediaType.contains(ConstantsHelper.MEDIA_TYPE_VIDEO) -> {
                            if (captionTextState.value.isNotBlank()) {
                                MediaTypeEnum.TextVideo.name
                            } else {
                                MediaTypeEnum.Video.name
                            }
                        }
                        // Otherwise, the story type is invalid.
                        else -> {
                            ""
                        }
                    }

                // Create a comma separated string from the caption offset values
                val captionOffset = "$captionOffsetX,$captionOffsetY"

                val backgroundColorGradient =
                    if (selectedMediaState.value == null) storyBackgroundColorState.value else FunctionHelper.getDefaultBackgroundGradient()
                val colorGradientString = backgroundColorGradient.joinToString {
                    it.toArgb().toHexString()
                }
                // Create a StoryBean object with the story details.
                val storyDetails = StoryBean(
                    "",
                    loggedInUserFirebaseId,
                    fileUrl,
                    captionTextState.value,
                    FunctionHelper.getCurrentTimeInMillis(),
                    storyType,
                    colorOnMedia.value.toArgb().toHexString(),
                    captionOffset,
                    colorGradientString,
                    selectedMediaState.value?.mediaDuration ?: 0,
                    false
                )

                // Upload the story details to the remote server.
                val serverResponse =
                    addStoryToRemoteUseCase.invoke(storyDetails)

                // Check if the upload operation was successful.
                if (serverResponse.status == RequestStatusEnum.Success) {
                    // Get the story ID from the response.
                    storyDetails.id = serverResponse.data ?: ""

                    // Set the upload story state to success.
                    _uploadStoryStateFlow.value = ResponseState.success(null)
                } else {
                    // Set the upload story state to error.
                    _uploadStoryStateFlow.value =
                        ResponseState.error(serverResponse.message ?: "")
                }
            }
        }
    }

}