package com.teamproject2k.connect.presentation.ui.chat.chat_details

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.teamproject2k.connect.domain.enums.MessageDeleteStatusEnum
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.chat.DeleteChatFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.chat.DeleteMessageOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.chat.LiveObserveChatListOnRemoteUseCase
import com.teamproject2k.connect.domain.use_case.chat.RemoveLiveObserveListenerFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.chat.SendMessageToRemoteUseCase
import com.teamproject2k.connect.domain.use_case.chat.UpdateLastSeenAtOnLocalUseCase
import com.teamproject2k.connect.domain.use_case.fcm.SendFCMUseCase
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.services.fcm.NotificationTypesEnum
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

    lateinit var loggedInUser: UserBean
    lateinit var otherUser: UserBean

    var isDataInitialized = false
    var listener: ChildEventListener? = null

    val isMessageSendingState = mutableStateOf(false)
    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    val onListenerErrorOccurredState = mutableStateOf("")
    val chatListState = mutableStateListOf<ChatBean>()
    var repliedOnChatState: MutableState<ChatBean?> = mutableStateOf(null)

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    private val _deleteMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val deleteMessageStateFlow = _deleteMessageStateFlow.asStateFlow()

    /**
     * Initializes data for the chat screen.
     * @param loggedInUserDetails Details of the logged-in user.
     * @param otherUserDetails Details of the other user in the chat.
     */
    fun initializeData(loggedInUserDetails: UserBean, otherUserDetails: UserBean) {
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

    /**
     * Sends a message to another user.
     * @param context The application context.
     */
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
                            loggedInUser.name
                        ),
                        Pair(
                            NotificationsConstantHelper.MESSAGE,
                            message.message
                        ),
                        Pair(
                            NotificationTypesEnum::name.name,
                            NotificationTypesEnum.ChatMessages.name
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

    /**
     * Initiates a live observation of the chat between the logged-in user and another user on the remote server.
     * This function sets up a listener to observe changes in the chat list and updates the UI accordingly.
     */
    fun liveObserveChat() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Initiating a live observation of the chat list on the remote server.
                // The listener observes changes in the chat list and updates the chatListState accordingly.
                listener = liveObserveChatListOnRemoteUseCase(
                    loggedInUser.firebaseUserId,
                    otherUser.firebaseUserId,
                    chatListState
                ) { errorMessage: String ->
                    // Handling errors that occur during the observation process by updating onListenerErrorOccurredState.
                    onListenerErrorOccurredState.value = errorMessage
                }
            }
        }
    }

    /**
     * Deletes a message from the remote server and updates the local database accordingly.
     * @param deletedBy The identifier of the user who initiated the deletion.
     * @param message The message to be deleted.
     */
    fun deleteMessage(deletedBy: String, message: ChatBean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteMessageStateFlow.value = ResponseState.loading() // Setting loading state.

                // Deleting the message from the remote server.
                val response = deleteMessageOnRemoteUseCase(
                    deletedBy,
                    message.senderId,
                    message.receiverId,
                    message.firebaseId
                )

                // If deletion on the remote server was successful, delete the message from the local database.
                if (response.status == RequestStatusEnum.Success) {
                    deleteChatFromLocalUseCase(message)
                }

                _deleteMessageStateFlow.value = response // Setting response state.
            }
        }
    }

    /**
     * Called when the ViewModel is being cleared and will no longer be used.
     * This function removes the live observation listener from the remote server
     * and updates the status indicating whether the chat detail screen is open in shared preferences.
     */
    override fun onCleared() {
        // Removing the live observation listener from the remote server, if it exists.
        listener?.let { removeLiveObserveListenerFromRemoteUseCase(it) }
    }
}
