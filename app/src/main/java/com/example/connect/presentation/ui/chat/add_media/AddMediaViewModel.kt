package com.example.connect.presentation.ui.chat.add_media

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.example.connect.domain.useCase.upload_file.UploadFileToRemoteUseCase
import com.example.connect.domain.utils.DomainFunctionHelper
import com.example.connect.domain.utils.FirebaseConstants
import com.example.connect.presentation.base.BaseViewModel
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ConstantsHelper
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
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase
) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")
    var isDataInitialized = false
    val isMessageSendingState = mutableStateOf(false)
    lateinit var loggedInUser: UsersBean
    lateinit var otherUser: UsersBean
    var repliedOnChatMediaState: MutableState<ChatBean?> = mutableStateOf(null)

    private val _sendMessageStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())
    val sendMessageStateFlow = _sendMessageStateFlow.asStateFlow()

    fun initializeData(
        message: String,
        loggedInUserDetails: UsersBean,
        otherUserDetails: UsersBean,
        repliedOnChatMedia: ChatBean?
    ) {
        this.loggedInUser = loggedInUserDetails
        this.otherUser = otherUserDetails
        messageState.value = message
        this.repliedOnChatMediaState.value = repliedOnChatMedia
        isDataInitialized = true
    }

    fun sendMessage(mediaData: MediaData) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendMessageStateFlow.value = ResponseState.loading()
                val response = uploadFileToRemoteUseCase(
                    mediaData.uri,
                    "${FirebaseConstants.CHATS_KEY}/${
                        DomainFunctionHelper.getSortedChatId(
                            loggedInUser.firebaseUserId,
                            otherUser.firebaseUserId
                        )
                    }/${FunctionHelper.getCurrentTimeInMillis()}"
                )
                if (response.status == RequestStatusEnum.Success && response.data != null) {
                    val mediaType = if (messageState.value.isNotBlank()) {
                        when (mediaData.mediaType) {
                            ConstantsHelper.MEDIA_TYPE_IMAGE -> {
                                MediaTypeEnum.TextImage.name
                            }

                            ConstantsHelper.MEDIA_TYPE_VIDEO -> {
                                MediaTypeEnum.TextVideo.name
                            }

                            else -> {
                                MediaTypeEnum.Text.name
                            }
                        }
                    } else if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
                        MediaTypeEnum.Image.name
                    } else if (mediaData.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
                        MediaTypeEnum.Video.name
                    } else {
                        ""
                    }
                    val sentAt = FunctionHelper.getCurrentTimeInMillis()
                    val message = ChatBean(
                        firebaseId = "",
                        senderId = loggedInUser.firebaseUserId,
                        receiverId = otherUser.firebaseUserId,
                        messageState.value,
                        sentAt,
                        sentAt,
                        MessageDeleteStatusEnum.DeletedForNone.name,
                        response.data,
                        mediaType,
                        repliedOnChatId = repliedOnChatMediaState.value?.firebaseId
                    )
                    _sendMessageStateFlow.value = sendMessageToRemoteUseCase(message)
                } else {
                    _sendMessageStateFlow.value = ResponseState.error(response.message ?: "")
                }
            }
        }
    }
}