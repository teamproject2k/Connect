package com.example.connect.presentation.ui.home.add_post

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.useCase.posts.AddPostToDbUseCase
import com.example.connect.domain.useCase.posts.UploadPostToRemoteUseCase
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.PostTypeEnum
import com.example.connect.presentation.ui.models.PostMediaData
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
    val selectedMediaState: MutableState<PostMediaData?> = mutableStateOf(null)
    lateinit var postVisibilityScopeList: List<VisibilityScope>
    lateinit var currentPostVisibilityState: MutableState<VisibilityScope>
    var isFirstTimeSetup = true
    val snackBarMessageState = mutableStateOf("")
    private val _uploadPostStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val uploadPostStateFlow: StateFlow<ResponseState<Nothing>> get() = _uploadPostStateFlow

    fun setUpData(context: Context) {
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        currentPostVisibilityState = mutableStateOf(postVisibilityScopeList[0])
        isFirstTimeSetup = false
    }

    fun uploadUserPost() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uploadPostStateFlow.value = ResponseState.loading()
                val firebaseId = fireBaseAuth.currentUser?.uid
                if (firebaseId != null) {
                    var fileUrl = ""
                    if (selectedMediaState.value != null) {
                        val uploadFileToRemoteResponse =
                            uploadFileToRemoteUseCase.invoke(
                                selectedMediaState.value!!.uri,
                                "${FirebaseConstants.POST_KEY}/$firebaseId/${System.currentTimeMillis()}"
                            )
                        if (uploadFileToRemoteResponse.status == RequestStatusEnum.EXCEPTION) {
                            _uploadPostStateFlow.value =
                                ResponseState.error(uploadFileToRemoteResponse.message ?: "")
                            return@withContext
                        } else {
                            fileUrl = uploadFileToRemoteResponse.data ?: ""
                        }
                    }
                    val postType =
                        when {
                            selectedMediaState.value == null -> {
                                PostTypeEnum.Text.name
                            }

                            selectedMediaState.value!!.mediaType.contains("image") -> {
                                if (captionTextState.value.isNotBlank()) {
                                    PostTypeEnum.TextImage.name
                                } else {
                                    PostTypeEnum.Image.name
                                }
                            }

                            selectedMediaState.value!!.mediaType.contains("video") -> {
                                if (captionTextState.value.isNotBlank()) {
                                    PostTypeEnum.TextVideo.name
                                } else {
                                    PostTypeEnum.Video.name
                                }
                            }

                            else -> {
                                ""
                            }
                        }
                    val postDetails = PostBean(
                        "",
                        firebaseId,
                        fileUrl,
                        captionTextState.value,
                        FunctionHelper.getCurrentTimeInMillis(),
                        currentPostVisibilityState.value.scopeEnum.name,
                        postType
                    )
                    val serverResponse = uploadPostToRemoteUseCase.invoke(postDetails, firebaseId)
                    if (serverResponse.status == RequestStatusEnum.SUCCESS) {
                        postDetails.id = serverResponse.data ?: ""
                        addPostToDbUseCase.invoke(postDetails)
                        _uploadPostStateFlow.value = ResponseState.success(null)
                    } else {
                        _uploadPostStateFlow.value =
                            ResponseState.error(serverResponse.message ?: "")
                    }
                } else {
                    _uploadPostStateFlow.value =
                        ResponseState.error(FirebaseErrorCodes.NO_USER_FOUND)
                }

            }
        }
    }
}