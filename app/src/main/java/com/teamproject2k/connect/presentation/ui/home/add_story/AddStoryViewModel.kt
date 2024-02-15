package com.teamproject2k.connect.presentation.ui.home.add_story

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.use_case.file.UploadFileToRemoteUseCase
import com.teamproject2k.connect.domain.use_case.story.AddStoryToLocalUseCase
import com.teamproject2k.connect.domain.use_case.story.AddStoryToRemoteUseCase
import com.teamproject2k.connect.domain.utils.FirebaseConstants
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.MediaTypeEnum
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString
import javax.inject.Inject

@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val addStoryToRemoteUseCase: AddStoryToRemoteUseCase,
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
    private val addStoryToLocalUseCase: AddStoryToLocalUseCase
) : BaseViewModel() {

    lateinit var colorOnMediaState: MutableState<Color>

    var isDataInitialized = false
    var isFirstTimePlaced = true
    var textColorList: List<Color> = FunctionHelper.getStoryTextColorList()
    var gradientColorList: List<List<Color>> = FunctionHelper.getStoryBackgroundColorList()

    val snackBarMessageState = mutableStateOf("")
    val captionTextState = mutableStateOf("")
    val captionOffsetXState = mutableFloatStateOf(0f)
    val captionOffsetYState = mutableFloatStateOf(0f)
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)
    var storyBackgroundColorState: MutableState<List<Color>> = mutableStateOf(gradientColorList[0])

    private val _uploadStoryStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadStoryStateFlow = _uploadStoryStateFlow.asStateFlow()

    fun initData(textColor: Color) {
        this.colorOnMediaState = mutableStateOf(textColor)
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
                        uploadFileToRemoteUseCase(
                            selectedMediaState.value!!.uri,
                            "${FirebaseConstants.STORY_KEY}/$loggedInUserFirebaseId/${FunctionHelper.getCurrentTimeInMillis()}"
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
                val captionOffset =
                    "${captionOffsetXState.floatValue},${captionOffsetYState.floatValue}"

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
                    colorOnMediaState.value.toArgb().toHexString(),
                    captionOffset,
                    colorGradientString,
                    selectedMediaState.value?.mediaDuration ?: 0,
                    arrayListOf(),
                    false
                )

                // Upload the story details to the remote server.
                val serverResponse =
                    addStoryToRemoteUseCase(storyDetails)

                // Check if the upload operation was successful.
                if (serverResponse.status == RequestStatusEnum.Success) {
                    // Get the story ID from the response.
                    storyDetails.storyFirebaseId = serverResponse.data ?: ""
                    addStoryToLocalUseCase(storyDetails)
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