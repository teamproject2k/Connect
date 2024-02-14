package com.teamproject2k.connect.presentation.ui.chat.add_media

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.enums.MessageDeleteStatusEnum
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.teamproject2k.connect.domain.useCase.fcm.SendFCMUseCase
import com.teamproject2k.connect.domain.useCase.file.UploadFileToRemoteUseCase
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import com.teamproject2k.connect.domain.utils.FirebaseConstants
import com.teamproject2k.connect.presentation.base.BaseViewModel
import com.teamproject2k.connect.presentation.ui.enums.MediaTypeEnum
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
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
class AddMediaViewModel @Inject constructor(
    private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase,
    private val uploadFileToRemoteUseCase: UploadFileToRemoteUseCase,
    private val sendFCMUseCase: SendFCMUseCase
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

    fun sendMessage(mediaData: MediaData, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _sendMessageStateFlow.value = ResponseState.loading()
                val uploadMediaResponse = uploadFileToRemoteUseCase(
                    mediaData.uri,
                    "${FirebaseConstants.CHATS_KEY}/${
                        DomainFunctionHelper.getSortedChatId(
                            loggedInUser.firebaseUserId,
                            otherUser.firebaseUserId
                        )
                    }/${FunctionHelper.getCurrentTimeInMillis()}"
                )
                if (uploadMediaResponse.status == RequestStatusEnum.Success && uploadMediaResponse.data != null) {
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
                        uploadMediaResponse.data,
                        mediaType,
                        repliedOnChatId = repliedOnChatMediaState.value?.firebaseId
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
                                message.message.ifBlank {
                                    mediaType.replace(
                                        MediaTypeEnum.Text.name,
                                        ""
                                    )
                                }
                            )
                        )
                        sendFCMUseCase(
                            FunctionHelper.getAccessToken(context),
                            data,
                            otherUser.fcmToken
                        )
                    }
                    _sendMessageStateFlow.value = response
                } else {
                    _sendMessageStateFlow.value =
                        ResponseState.error(uploadMediaResponse.message ?: "")
                }
            }
        }
    }
}