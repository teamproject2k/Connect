package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
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
class ChatDetailsViewModel @Inject constructor(private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase) :
    BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    val isMessageSendingState = mutableStateOf(false)
    val chatListState = mutableStateListOf<ChatBean>()

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    private val _getChatListStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    fun sendMessage(loggedInUserFirebaseId: String, otherUserFirebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendMessageStateFlow.value = ResponseState.loading()
                val sentAt = FunctionHelper.getCurrentTimeInMillis()
                val message = ChatBean(
                    firebaseId = "",
                    senderId = loggedInUserFirebaseId,
                    receiverId = otherUserFirebaseUserId,
                    messageState.value,
                    sentAt,
                    sentAt,
                    MessageDeleteStatusEnum.DeletedForNone.name,
                    "",
                    MediaTypeEnum.Text.name
                )
                _sendMessageStateFlow.value = sendMessageToRemoteUseCase(message)
            }
        }
    }

    fun liveObserveChat() {

    }
}