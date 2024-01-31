package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.DeleteMessageOnRemoteUseCase
import com.example.connect.domain.useCase.chat.LiveObserveChatListOnRemoteUseCase
import com.example.connect.domain.useCase.chat.RemoveLiveObserveListenerFromRemoteUseCase
import com.example.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.example.connect.domain.useCase.user.UpdateUserLastActiveAtChatOnRemoteUseCase
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
    private val removeLiveObserveListenerFromRemoteUseCase: RemoveLiveObserveListenerFromRemoteUseCase,
    private val deleteMessageOnRemoteUseCase: DeleteMessageOnRemoteUseCase,
    private val updateUserLastActiveAtChatOnRemoteUseCase: UpdateUserLastActiveAtChatOnRemoteUseCase
) :
    BaseViewModel() {

    var isDataInitialized = false
    var listener: ChildEventListener? = null
    lateinit var loggedInUser: UsersBean
    val isMessageSendingState = mutableStateOf(false)
    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    val onListenerErrorOccurredState = mutableStateOf("")
    val chatListState = mutableStateListOf<ChatBean>()

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    private val _deleteMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deleteMessageStateFlow = _deleteMessageStateFlow.asStateFlow()

    var repliedOnChatState: MutableState<ChatBean?> = mutableStateOf(null)

    fun sendMessage(otherUserFirebaseUserId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendMessageStateFlow.value = ResponseState.loading()
                val sentAt = FunctionHelper.getCurrentTimeInMillis()
                val message = ChatBean(
                    firebaseId = "",
                    senderId = loggedInUser.firebaseUserId,
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

    fun liveObserveChat(otherUserFirebaseUserId: String) {
        listener = liveObserveChatListOnRemoteUseCase(
            loggedInUser.firebaseUserId,
            otherUserFirebaseUserId,
            chatListState
        ) { errorMessage: String ->
            onListenerErrorOccurredState.value = errorMessage
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.let { removeLiveObserveListenerFromRemoteUseCase(it) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val updatedLastActiveAt = FunctionHelper.getCurrentTimeInMillis()
                val response = updateUserLastActiveAtChatOnRemoteUseCase(
                    updatedLastActiveAt,
                    loggedInUser.firebaseUserId
                )
                if (response.status == RequestStatusEnum.Success) {
                    loggedInUser.lastActiveAt = updatedLastActiveAt

                }
            }
        }
    }

    fun deleteMessage(deletedBy: String, message: ChatBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteMessageStateFlow.value = ResponseState.loading()
                val response =
                    deleteMessageOnRemoteUseCase(
                        deletedBy,
                        message.senderId,
                        message.receiverId,
                        message.firebaseId
                    )
                if (response.status == RequestStatusEnum.Success) {
                    message.deletedBy = deletedBy
                }
                _deleteMessageStateFlow.value = response
            }
        }
    }
}
