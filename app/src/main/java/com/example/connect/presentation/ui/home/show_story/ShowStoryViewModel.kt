package com.example.connect.presentation.ui.home.show_story

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.story.DeleteStoryInRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StateNameRule")
@HiltViewModel
class ShowStoryViewModel @Inject constructor(private val deleteStoryInRemoteUseCase: DeleteStoryInRemoteUseCase) :
    BaseViewModel() {

    private val _deleteStoryStateFlow: MutableStateFlow<ResponseState<List<UsersBean>>> =
        MutableStateFlow(ResponseState.none())
    val deleteStoryStateFlow: StateFlow<ResponseState<List<UsersBean>>> get() = _deleteStoryStateFlow

    val snackBarMessageState = mutableStateOf("")
    var areDetailsInitialized = false

    val isDropdownMenuVisibleState = mutableStateOf(false)

    lateinit var allUsersStories: MutableMap<String, ArrayList<StoryBean>>

    lateinit var allUsersList: MutableList<UsersBean>

    lateinit var currentStoryIndex: MutableIntState
    lateinit var storyVisibleForUserId: MutableState<String>
    val mapKeyList = mutableListOf<String>()

    fun init(
        userStoriesString: String,
        allUsersList: MutableList<UsersBean>,
        initialStoryForUserId: String
    ) {
        allUsersStories = Gson().fromJson(
            userStoriesString,
            object : TypeToken<MutableMap<String, ArrayList<StoryBean>>>() {}.type
        )
        this.allUsersList = allUsersList
        mapKeyList.addAll(allUsersStories.keys.toMutableList())
        this.storyVisibleForUserId = mutableStateOf(initialStoryForUserId)
        currentStoryIndex = mutableIntStateOf(0)
        areDetailsInitialized = true
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteStoryStateFlow.value = ResponseState.loading()
                _deleteStoryStateFlow.value = deleteStoryInRemoteUseCase(storyId)
            }
        }
    }
}