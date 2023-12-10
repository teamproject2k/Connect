package com.example.connect.presentation.ui.home.add_post

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.common.RequestStatusEnum
import com.example.connect.common.ResponseState
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.models.PostMediaData
import com.example.connect.presentation.ui.models.PostVisibilityScope
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
class AddPostViewModel @Inject constructor(private val useCase: HomeUseCase) : BaseViewModel() {
    val captionTextState = mutableStateOf("")
    val selectedMediaState: MutableState<PostMediaData?> = mutableStateOf(null)
    lateinit var postVisibilityScopeList: List<PostVisibilityScope>
    lateinit var currentPostVisibilityState: MutableState<PostVisibilityScope>
    var isFirstTimeSetup = true
    val snackBarMessageState = mutableStateOf("")
    private val _uploadPostStateFlow: MutableStateFlow<ResponseState<String>> =
        MutableStateFlow(ResponseState.none())
    private val uploadPostStateFlow: StateFlow<ResponseState<String>> get() = _uploadPostStateFlow

    fun setUpData(context: Context) {
        postVisibilityScopeList = FunctionHelper.getPostVisibilityList(context)
        currentPostVisibilityState = mutableStateOf(postVisibilityScopeList[0])
        isFirstTimeSetup = false
    }


    fun uploadUserPost(firebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uploadPostStateFlow.value = ResponseState.loading()
                if (selectedMediaState.value != null) {

                }
                val postDetails = PostDetails(
                    "",
                    firebaseId,
                    "",
                    captionTextState.value,
                    FunctionHelper.getCurrentTimeInMillis(),
                    "",
                    ""
                )
                val serverResponse = useCase.uploadPostToServer(postDetails, firebaseId)
                if (serverResponse.status == RequestStatusEnum.SUCCESS) {

                }
            }
        }
    }
}