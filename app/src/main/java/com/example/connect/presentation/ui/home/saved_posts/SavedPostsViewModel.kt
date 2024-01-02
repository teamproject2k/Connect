package com.example.connect.presentation.ui.home.saved_posts

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.posts.GetSavedPostsFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(private val getSavedPostsFromRemoteUseCase: GetSavedPostsFromRemoteUseCase) :
    BaseViewModel() {

    private val _getSavedPostsStateFlow: MutableStateFlow<ResponseState<List<PostBean>>> =
        MutableStateFlow(ResponseState.none())
    val getSavedPostsStateFlow: StateFlow<ResponseState<List<PostBean>>> get() = _getSavedPostsStateFlow

    val snackBarMessageState = mutableStateOf("")

    fun getSavedPosts(savedPosts: ArrayList<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getSavedPostsStateFlow.value = ResponseState.loading()
                _getSavedPostsStateFlow.value = getSavedPostsFromRemoteUseCase.invoke(savedPosts)
            }
        }
    }
}