package com.example.connect.presentation.ui.home.show_story

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.StorySeenByBean
import com.example.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.story.AddUserToSeenListInRemoteUseCase
import com.example.connect.domain.useCase.story.DeleteStoryFromLocalUseCase
import com.example.connect.domain.useCase.story.DeleteStoryInRemoteUseCase
import com.example.connect.domain.useCase.story.GetSeenListFromRemoteUseCase
import com.example.connect.domain.useCase.story.UpdateStoryOnLocalUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.utils.ConstantsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShowStoryViewModel @Inject constructor(
    private val deleteStoryInRemoteUseCase: DeleteStoryInRemoteUseCase,
    private val deleteStoryFromLocalUseCase: DeleteStoryFromLocalUseCase,
    private val getSeenListFromRemoteUseCase: GetSeenListFromRemoteUseCase,
    private val addUserToSeenListInRemoteUseCase: AddUserToSeenListInRemoteUseCase,
    private val updateStoryOnLocalUseCase: UpdateStoryOnLocalUseCase
) :
    BaseViewModel() {

    private val _getSeenListStateFlow: MutableStateFlow<ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>>> =
        MutableStateFlow(ResponseState.none())
    val getSeenListStateFlow = _getSeenListStateFlow.asStateFlow()

    private val _addUserToSeenListStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    private val _deleteStoryStateFlow: MutableStateFlow<ResponseState<Boolean>> =
        MutableStateFlow(ResponseState.none())
    val deleteStoryStateFlow = _deleteStoryStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")
    var areDetailsInitialized = false

    var showStorySeenListBottomSheetState = mutableStateOf(false)

    val isDropdownMenuVisibleState = mutableStateOf(false)

    lateinit var allStoriesWithUsersList: ArrayList<StoriesWithUserBean>
    lateinit var currentStoryIndexState: MutableIntState
    lateinit var userStoriesIndexState: MutableIntState

    val pauseTimerState = mutableStateOf(false)

    fun init(
        allBeanStoriesWithUsersList: ArrayList<StoriesWithUserBean>,
        currentStoryIndex: Int
    ) {
        this.allStoriesWithUsersList = allBeanStoriesWithUsersList
        this.userStoriesIndexState = mutableIntStateOf(currentStoryIndex)
        this.currentStoryIndexState = mutableIntStateOf(0)
        areDetailsInitialized = true
    }

    fun getSeenList(storyId: String, loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSeenListStateFlow.value = ResponseState.loading()
                _getSeenListStateFlow.value =
                    getSeenListFromRemoteUseCase(storyId, loggedInUserFirebaseId)
            }
        }
    }

    fun addUserToSeenList(story: StoryBean, loggedInUserFirebaseId: String, seenAt: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _addUserToSeenListStateFlow.value = ResponseState.loading()
                val response =
                    addUserToSeenListInRemoteUseCase(
                        story.storyFirebaseId,
                        loggedInUserFirebaseId,
                        seenAt
                    )
                if (response.status == RequestStatusEnum.Exception) {
                    LoggingHelper.logData(
                        LoggingLevelEnum.Error,
                        ConstantsHelper.ERROR_TAG,
                        ScreenNameEnum.ShowStoryViewModel.name,
                        response.message.toString()
                    )
                } else {
                    story.seenBy.add(StorySeenByBean(loggedInUserFirebaseId, seenAt))
                    updateStoryOnLocalUseCase(story)
                }
                _addUserToSeenListStateFlow.value = response
            }
        }
    }

    fun deleteStory(story: StoryBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteStoryStateFlow.value = ResponseState.loading()
                val response = deleteStoryInRemoteUseCase(story.storyFirebaseId)
                if (response.status == RequestStatusEnum.Success) {
                    deleteStoryFromLocalUseCase(story.storyFirebaseId)
                    withContext(Dispatchers.Main) {
                        val storyList =
                            allStoriesWithUsersList[userStoriesIndexState.intValue].storiesList
                        if (storyList.size == 1) {
                            _deleteStoryStateFlow.value = ResponseState.success(true)
                        } else {
                            val storyIndex = storyList.indexOf(story)
                            if (storyIndex == storyList.lastIndex) {
                                currentStoryIndexState.intValue--
                            } else {
                                currentStoryIndexState.intValue++
                            }
                            _deleteStoryStateFlow.value = ResponseState.success(false)
                        }
                    }
                } else {
                    _deleteStoryStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }
}