package com.example.connect.presentation.ui.chat.chat_list

import androidx.compose.runtime.mutableStateOf
import com.example.connect.domain.models.ChatWithUserDetails
import com.example.connect.domain.models.ChatsBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor() : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getChatListStateFlow: MutableStateFlow<ResponseState<MutableList<ChatWithUserDetails>>> =
        MutableStateFlow(ResponseState.none())

    val getChatListStateFlow = _getChatListStateFlow.asStateFlow()
}