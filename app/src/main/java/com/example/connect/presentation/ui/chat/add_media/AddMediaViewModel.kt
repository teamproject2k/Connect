package com.example.connect.presentation.ui.chat.add_media

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.FunctionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddMediaViewModel @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    var isDataInitialized = false
    val isMessageSendingState = mutableStateOf(false)
    lateinit var loggedInUser: UsersBean
    lateinit var otherUser: UsersBean

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    fun initializeData(
        message: String,
        loggedInUserDetails: UsersBean,
        otherUserDetails: UsersBean
    ) {
        this.loggedInUser = loggedInUserDetails
        this.otherUser = otherUserDetails
        messageState.value = message
        isDataInitialized = true
    }

    fun sendMessage() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendMessageStateFlow.value = ResponseState.loading()
                val sentAt = FunctionHelper.getCurrentTimeInMillis()
                val message = ChatBean(
                    firebaseId = "",
                    senderId = loggedInUser.firebaseUserId,
                    receiverId = otherUser.firebaseUserId,
                    messageState.value,
                    sentAt,
                    sentAt,
                    MessageDeleteStatusEnum.DeletedForNone.name,
                    "",
                    MediaTypeEnum.Text.name,
                    repliedOnChatId = "repliedOnChatState.value?.firebaseId"
                )
                _sendMessageStateFlow.value = sendMessageToRemoteUseCase(message)
            }
        }
    }
}