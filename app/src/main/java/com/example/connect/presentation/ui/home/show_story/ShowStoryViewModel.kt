package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.story.AddUserToSeenListInRemoteUseCase
import com.example.connect.domain.useCase.story.DeleteStoryInRemoteUseCase
import com.example.connect.domain.useCase.story.GetSeenListFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class ShowStoryViewModel @Inject constructor(
    private val addUserToSeenListInRemoteUseCase: AddUserToSeenListInRemoteUseCase,
    private val getSeenListFromRemoteUseCase: GetSeenListFromRemoteUseCase,
    private val deleteStoryInRemoteUseCase: DeleteStoryInRemoteUseCase
) :
    BaseViewModel() {
    val snackBarMessageState = mutableStateOf("")
    val currentStoryState = mutableIntStateOf(0)
    lateinit var currentStoryPosterState: MutableState<UsersBean>
    var isCurrentStoryPosterInitialized = false

    private val _getSeenListStateFlow: MutableStateFlow<ResponseState<List<Pair<String, Long>>>> =
        MutableStateFlow(ResponseState.none())
    val getSeenListStateFlow: StateFlow<ResponseState<List<Pair<String, Long>>>> get() = _getSeenListStateFlow

    private val _deleteStoryStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deleteStoryStateFlow: StateFlow<ResponseState<Nothing>> get() = _deleteStoryStateFlow

    var showSeenListBottomSheet = mutableStateOf(false)

    var showDeleteStoryAlertDialog = mutableStateOf(false)


    fun initData(currentStoryPoster: UsersBean) {
        currentStoryPosterState.value = currentStoryPoster
        isCurrentStoryPosterInitialized = true
    }

    fun addUserToSeenList(storyId: String, loggedInUserFireBaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addUserToSeenListInRemoteUseCase.invoke(storyId, loggedInUserFireBaseId)
            }
        }
    }

    fun getSeenList(storyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSeenListStateFlow.value = ResponseState.loading()
                _getSeenListStateFlow.value = getSeenListFromRemoteUseCase.invoke(storyId)
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteStoryStateFlow.value = ResponseState.loading()
                _deleteStoryStateFlow.value = deleteStoryInRemoteUseCase.invoke(storyId)
            }
        }
    }
}