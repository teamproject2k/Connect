package com.teamproject2k.connect.presentation.ui.home.show_story

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.models.StorySeenByBean
import com.teamproject2k.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.story.AddUserToSeenListInRemoteUseCase
import com.teamproject2k.connect.domain.use_case.story.DeleteStoryFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.story.DeleteStoryInRemoteUseCase
import com.teamproject2k.connect.domain.use_case.story.GetSeenListFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.story.UpdateStoryOnLocalUseCase
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
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

    lateinit var allStoriesWithUsersList: ArrayList<StoriesWithUserBean>
    lateinit var currentStoryIndexState: MutableIntState
    lateinit var userStoriesIndexState: MutableIntState

    var isDataInitialized = false

    val snackBarMessageState = mutableStateOf("")
    var showStorySeenListBottomSheetState = mutableStateOf(false)
    val isDropdownMenuVisibleState = mutableStateOf(false)
    val pauseTimerState = mutableStateOf(false)

    private val _getSeenListStateFlow: MutableStateFlow<ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>>> =
        MutableStateFlow(ResponseState.none())
    val getSeenListStateFlow = _getSeenListStateFlow.asStateFlow()

    private val _addUserToSeenListStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    private val _deleteStoryStateFlow: MutableStateFlow<ResponseState<Boolean>> =
        MutableStateFlow(ResponseState.none())
    val deleteStoryStateFlow = _deleteStoryStateFlow.asStateFlow()

    /**
     * Initializes the data for managing stories.
     * This function sets the provided list of stories with user details and the current story index.
     *
     * @param allBeanStoriesWithUsersList The list of stories with user details.
     * @param currentStoryIndex The index of the currently displayed story.
     */
    fun initializeData(
        allBeanStoriesWithUsersList: ArrayList<StoriesWithUserBean>,
        currentStoryIndex: Int
    ) {
        this.allStoriesWithUsersList = allBeanStoriesWithUsersList
        this.userStoriesIndexState = mutableIntStateOf(currentStoryIndex)
        this.currentStoryIndexState = mutableIntStateOf(0)
        isDataInitialized = true
    }

    /**
     * Retrieves the seen list of a story.
     * This function fetches the seen list of a story from the remote server.
     *
     * @param storyId The Firebase ID of the story for which the seen list is being retrieved.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     */
    fun getSeenList(storyId: String, loggedInUserFirebaseId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSeenListStateFlow.value = ResponseState.loading()
                _getSeenListStateFlow.value =
                    getSeenListFromRemoteUseCase(storyId, loggedInUserFirebaseId)
            }
        }
    }

    /**
     * Adds a user to the seen list of a story.
     * This function adds a user to the seen list of a story, updating both the remote server and local storage.
     *
     * @param story The story for which the user is being added to the seen list.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @param seenAt The timestamp indicating when the user viewed the story.
     */
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

    /**
     * Deletes a story from the remote server and local storage.
     * This function deletes a story from the remote server and local storage.
     *
     * @param story The story to be deleted.
     */
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