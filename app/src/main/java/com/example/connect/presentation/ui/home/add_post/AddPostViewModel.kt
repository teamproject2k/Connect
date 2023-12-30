package com.example.connect.presentation.ui.home.add_post

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.AddPostToDbUseCase
import com.example.connect.domain.useCase.posts.UploadPostToRemoteUseCase
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.ui.models.VisibilityScope
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
@SuppressLint("StateNameRule")
class AddPostViewModel @Inject constructor(
    private val uploadPostToRemoteUseCase: UploadPostToRemoteUseCase,
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
    private val addPostToDbUseCase: AddPostToDbUseCase
) : BaseViewModel() {
    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<MediaData?> = mutableStateOf(null)
    lateinit var postVisibilityScopeList: List<VisibilityScope>
    lateinit var currentPostVisibilityState: MutableState<VisibilityScope>
    var isFirstTimeSetup = true
    val snackBarMessageState = mutableStateOf("")
    private val _uploadPostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadPostStateFlow: StateFlow<ResponseState<Nothing>> get() = _uploadPostStateFlow

    /**
     * Sets up the data for the app.
     *
     * @param context The context of the app.
     */
    fun setUpData(context: Context) {
        // Get the list of post visibility scopes from the context.
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)

        // Set the current post visibility state to the first item in the list.
        currentPostVisibilityState = mutableStateOf(postVisibilityScopeList[0])

        // Set the isFirstTimeSetup flag to false.
        isFirstTimeSetup = false
    }

    fun uploadUserPost(currentUserFirebaseId: String) {
        // Launch a coroutine in the viewModelScope.
        viewModelScope.launch {
            // Perform the upload operation in the IO dispatcher.
            withContext(Dispatchers.IO) {
                // Set the upload post state to loading.
                _uploadPostStateFlow.value = ResponseState.loading()
                // Initialize the file URL to an empty string.
                var fileUrl = ""

                // Check if the user has selected any media.
                if (selectedMediaState.value != null) {
                    // Upload the selected media to the remote server.
                    val uploadFileToRemoteResponse =
                        uploadFileToRemoteUseCase.invoke(
                            selectedMediaState.value!!.uri,
                            "${FirebaseConstants.POST_KEY}/$currentUserFirebaseId/${System.currentTimeMillis()}"
                        )

                    // Check if the upload operation was successful.
                    if (uploadFileToRemoteResponse.status == RequestStatusEnum.Exception) {
                        // Set the upload post state to error.
                        _uploadPostStateFlow.value =
                            ResponseState.error(uploadFileToRemoteResponse.message ?: "")
                        return@withContext
                    } else {
                        // Get the file URL from the response.
                        fileUrl = uploadFileToRemoteResponse.data ?: ""
                    }
                }

                // Determine the post type based on the selected media and caption text.
                val postType =
                    when {
                        // If no media is selected, the post type is Text.
                        selectedMediaState.value == null -> {
                            MediaTypeEnum.Text.name
                        }

                        // If the selected media is an image, the post type is Image or TextImage.
                        selectedMediaState.value!!.mediaType.contains("image") -> {
                            if (captionTextState.value.isNotBlank()) {
                                MediaTypeEnum.TextImage.name
                            } else {
                                MediaTypeEnum.Image.name
                            }
                        }

                        // If the selected media is a video, the post type is Video or TextVideo.
                        selectedMediaState.value!!.mediaType.contains("video") -> {
                            if (captionTextState.value.isNotBlank()) {
                                MediaTypeEnum.TextVideo.name
                            } else {
                                MediaTypeEnum.Video.name
                            }
                        }

                        // Otherwise, the post type is invalid.
                        else -> {
                            ""
                        }
                    }

                // Create a PostBean object with the post details.
                val postDetails = PostBean(
                    "",
                    currentUserFirebaseId,
                    fileUrl,
                    captionTextState.value,
                    FunctionHelper.getCurrentTimeInMillis(),
                    currentPostVisibilityState.value.scopeEnum.name,
                    postType,
                    0,
                    false,
                    arrayListOf()
                )

                // Upload the post details to the remote server.
                val serverResponse =
                    uploadPostToRemoteUseCase.invoke(postDetails, currentUserFirebaseId)

                // Check if the upload operation was successful.
                if (serverResponse.status == RequestStatusEnum.Success) {
                    // Get the post ID from the response.
                    postDetails.id = serverResponse.data ?: ""

                    // Add the post to the local database.
                    addPostToDbUseCase.invoke(postDetails)

                    // Set the upload post state to success.
                    _uploadPostStateFlow.value = ResponseState.success(null)
                } else {
                    // Set the upload post state to error.
                    _uploadPostStateFlow.value =
                        ResponseState.error(serverResponse.message ?: "")
                }
            }
        }
    }
}