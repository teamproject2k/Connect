package com.example.connect.presentation.ui.chat.chat_list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.ChatWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.GetChatListFromRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(private val getChatListFromRemoteUseCase: GetChatListFromRemoteUseCase) :
    BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getChatListStateFlow: MutableStateFlow<ResponseState<MutableList<ChatWithUserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val getChatListStateFlow = _getChatListStateFlow.asStateFlow()

    var isDetailsInitialized = false
    lateinit var loggedInUserDetails: UsersBean

    fun initData(loggedInUserDetails: UsersBean) {
        this.loggedInUserDetails = loggedInUserDetails
        isDetailsInitialized = true
    }

    fun getChatList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getChatListStateFlow.value = ResponseState.loading()
                _getChatListStateFlow.value = ResponseState.success(null)
                // _getChatListStateFlow.value=getChatListFromRemoteUseCase(loggedInUserDetails.firebaseUserId)
            }
        }
    }

}