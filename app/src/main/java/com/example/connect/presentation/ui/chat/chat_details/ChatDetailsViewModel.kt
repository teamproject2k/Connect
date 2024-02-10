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
import com.example.connect.domain.useCase.chat.UpdateLastSeenAtOnLocalUseCase
import com.example.connect.domain.utils.DomainFunctionHelper
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.utils.FunctionHelper
import com.google.firebase.database.ChildEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val liveObserveChatListOnRemoteUseCase: LiveObserveChatListOnRemoteUseCase,
    private val removeLiveObserveListenerFromRemoteUseCase: RemoveLiveObserveListenerFromRemoteUseCase,
    private val deleteMessageOnRemoteUseCase: DeleteMessageOnRemoteUseCase,
    private val updateLastSeenAtOnLocalUseCase: UpdateLastSeenAtOnLocalUseCase
) :
    BaseViewModel() {

    var isDataInitialized = false
    var listener: ChildEventListener? = null
    lateinit var loggedInUser: UsersBean
    lateinit var otherUser: UsersBean
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
                    repliedOnChatId = repliedOnChatState.value?.firebaseId
                )
                _sendMessageStateFlow.value = sendMessageToRemoteUseCase(message)
            }
        }
    }

    fun liveObserveChat() {
        listener = liveObserveChatListOnRemoteUseCase(
            loggedInUser.firebaseUserId,
            otherUser.firebaseUserId,
            chatListState
        ) { errorMessage: String ->
            onListenerErrorOccurredState.value = errorMessage
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        listener?.let { removeLiveObserveListenerFromRemoteUseCase(it) }
        runBlocking {
            GlobalScope.launch {
                updateLastSeenAtOnLocalUseCase(
                    DomainFunctionHelper.getSortedChatId(
                        loggedInUser.firebaseUserId,
                        otherUser.firebaseUserId
                    ),
                    FunctionHelper.getCurrentTimeInMillis()
                )
            }.join()
        }
        super.onCleared()
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
