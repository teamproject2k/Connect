package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.LiveObserveChatListOnRemoteUseCase
import com.example.connect.domain.useCase.chat.RemoveLiveObserveListenerFromRemoteUseCase
import com.example.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.google.firebase.database.ChildEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val liveObserveChatListOnRemoteUseCase: LiveObserveChatListOnRemoteUseCase,
    private val removeLiveObserveListenerFromRemoteUseCase: RemoveLiveObserveListenerFromRemoteUseCase
) :
    BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    val isMessageSendingState = mutableStateOf(false)
    val chatListState = mutableStateListOf<ChatBean>()

    var listener: ChildEventListener? = null

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    val onListenerErrorOccurredState = mutableStateOf("")

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

    fun liveObserveChat(loggedInUserFirebaseId: String, otherUserFirebaseUserId: String) {
        listener = liveObserveChatListOnRemoteUseCase(
            loggedInUserFirebaseId,
            otherUserFirebaseUserId,
            chatListState
        ) { errorMessage: String ->
            onListenerErrorOccurredState.value = errorMessage
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.let { removeLiveObserveListenerFromRemoteUseCase(it) }
    }
}