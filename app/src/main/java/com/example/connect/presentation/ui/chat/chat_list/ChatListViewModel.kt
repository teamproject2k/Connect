package com.example.connect.presentation.ui.chat.chat_list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.ChatMetaDataBean
import com.example.connect.domain.models.ChatWithUserAndCountBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.useCase.chat.AddChatMessagesListToLocalUseCase
import com.example.connect.domain.useCase.chat.AddChatMetaDataListToLocalUseCase
import com.example.connect.domain.useCase.chat.GetChatListFromRemoteUseCase
import com.example.connect.domain.useCase.chat.GetUserWithLastMessageWithUnreadCountFromLocalUseCase
import com.example.connect.domain.useCase.user.AddUserListToLocalUseCase
import com.example.connect.domain.utils.DomainFunctionHelper
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatListFromRemoteUseCase: GetChatListFromRemoteUseCase,
    private val addChatMetaDataListToLocalUseCase: AddChatMetaDataListToLocalUseCase,
    private val addChatMessagesListToLocalUseCase: AddChatMessagesListToLocalUseCase,
    private val addUserListToLocalUseCase: AddUserListToLocalUseCase,
    private val getUserWithLastMessageWithUnreadCountFromLocalUseCase: GetUserWithLastMessageWithUnreadCountFromLocalUseCase

) : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")

    private val _getChatListStateFlow: MutableStateFlow<ResponseState<MutableList<ChatWithUserAndCountBean>>> =
        MutableStateFlow(ResponseState.none())

    val getChatListStateFlow = _getChatListStateFlow.asStateFlow()

    var isDetailsInitialized = false
    lateinit var loggedInUserDetails: UsersBean


    private var isChatListFetched: Boolean = false

    fun initData(loggedInUserDetails: UsersBean) {
        this.loggedInUserDetails = loggedInUserDetails
        isDetailsInitialized = true
    }

    fun getChatList(isForceUpdate: Boolean, isNetworkAvailable: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _getChatListStateFlow.value = ResponseState.loading()
                if (isNetworkAvailable && (isForceUpdate || !isChatListFetched)) {
                    val response =
                        getChatListFromRemoteUseCase(loggedInUserDetails.firebaseUserId)
                    if (response.status == RequestStatusEnum.Success) {
                        if (response.data != null) {
                            val chatMetaDataBeanList = response.data.map {
                                ChatMetaDataBean(
                                    DomainFunctionHelper.getSortedChatId(
                                        loggedInUserDetails.firebaseUserId,
                                        it.userDetails.firebaseUserId
                                    ), 0, false
                                )
                            }
                            val chatMessageList = response.data.flatMap {
                                it.chatList
                            }
                            addChatMetaDataListToLocalUseCase(chatMetaDataBeanList)
                            addChatMessagesListToLocalUseCase(chatMessageList)
                            addUserListToLocalUseCase(response.data.map { it.userDetails })
                        }
                        isChatListFetched = true
                        _getChatListStateFlow.value =
                            ResponseState.success(
                                getUserWithLastMessageWithUnreadCountFromLocalUseCase(
                                    loggedInUserDetails.firebaseUserId
                                ).toMutableList()
                            )
                    } else {
                        _getChatListStateFlow.value = ResponseState.error(response.message ?: "")
                    }
                } else {
                    _getChatListStateFlow.value =
                        ResponseState.success(
                            getUserWithLastMessageWithUnreadCountFromLocalUseCase(
                                loggedInUserDetails.firebaseUserId
                            ).toMutableList()
                        )
                }
            }
        }
    }

}