package com.teamproject2k.connect.presentation.ui.chat.chat_details

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.enums.MessageDeleteStatusEnum
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.useCase.chat.DeleteChatFromLocalUseCase
import com.teamproject2k.connect.domain.useCase.chat.DeleteMessageOnRemoteUseCase
import com.teamproject2k.connect.domain.useCase.chat.LiveObserveChatListOnRemoteUseCase
import com.teamproject2k.connect.domain.useCase.chat.RemoveLiveObserveListenerFromRemoteUseCase
import com.teamproject2k.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.teamproject2k.connect.domain.useCase.chat.UpdateLastSeenAtOnLocalUseCase
import com.teamproject2k.connect.domain.useCase.fcm.SendFCMUseCase
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.MediaTypeEnum
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.NotificationsConstantHelper
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
    private val updateLastSeenAtOnLocalUseCase: UpdateLastSeenAtOnLocalUseCase,
    private val deleteChatFromLocalUseCase: DeleteChatFromLocalUseCase,
    private val sendFCMUseCase: SendFCMUseCase
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

    init {
        sharedPreference.isChatDetailScreenOpen = true
    }

    fun initializeData(loggedInUserDetails: UsersBean, otherUserDetails: UsersBean) {
        this.loggedInUser = loggedInUserDetails
        this.otherUser = otherUserDetails
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateLastSeenAtOnLocalUseCase(
                    DomainFunctionHelper.getSortedChatId(
                        loggedInUser.firebaseUserId,
                        otherUser.firebaseUserId
                    ),
                    FunctionHelper.getCurrentTimeInMillis()
                )
            }
        }
        isDataInitialized = true
    }

    fun sendMessage(context: Context) {
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
                val response = sendMessageToRemoteUseCase(message)
                if (response.status == RequestStatusEnum.Success) {
                    val data = hashMapOf(
                        Pair(
                            NotificationsConstantHelper.TITLE,
                            otherUser.name
                        ),
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            message.message
                        )
                    )
                    sendFCMUseCase(
                        FunctionHelper.getAccessToken(context),
                        data,
                        otherUser.fcmToken
                    )
                }
                _sendMessageStateFlow.value = response
            }
        }
    }

    fun liveObserveChat() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                listener = liveObserveChatListOnRemoteUseCase(
                    loggedInUser.firebaseUserId,
                    otherUser.firebaseUserId,
                    chatListState
                ) { errorMessage: String ->
                    onListenerErrorOccurredState.value = errorMessage
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
                    deleteChatFromLocalUseCase(message)
                }
                _deleteMessageStateFlow.value = response
            }
        }
    }

    override fun onCleared() {
        listener?.let { removeLiveObserveListenerFromRemoteUseCase(it) }
        sharedPreference.isChatDetailScreenOpen = false
    }
}
