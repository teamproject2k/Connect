package com.example.connect.presentation.ui.home.show_story

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.story.DeleteStoryFromLocalUseCase
import com.example.connect.domain.useCase.story.DeleteStoryInRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
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
    private val deleteStoryFromLocalUseCase: DeleteStoryFromLocalUseCase
) :
    BaseViewModel() {

    private val _deleteStoryStateFlow: MutableStateFlow<ResponseState<Boolean>> =
        MutableStateFlow(ResponseState.none())
    val deleteStoryStateFlow = _deleteStoryStateFlow.asStateFlow()

    val snackBarMessageState = mutableStateOf("")
    var areDetailsInitialized = false

    val isDropdownMenuVisibleState = mutableStateOf(false)

    lateinit var allStoriesWithUsersList: ArrayList<StoriesWithUserBean>
    lateinit var currentStoryIndexState: MutableIntState
    lateinit var currentUserStoriesIndexState: MutableIntState

    fun init(
        allBeanStoriesWithUsersList: ArrayList<StoriesWithUserBean>,
        currentStoryIndex: Int
    ) {
        this.allStoriesWithUsersList = allBeanStoriesWithUsersList
        this.currentUserStoriesIndexState = mutableIntStateOf(currentStoryIndex)
        this.currentStoryIndexState = mutableIntStateOf(0)
        areDetailsInitialized = true
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
                            allStoriesWithUsersList[currentUserStoriesIndexState.intValue].storiesList
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