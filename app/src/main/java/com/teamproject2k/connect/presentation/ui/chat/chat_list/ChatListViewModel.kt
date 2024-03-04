package com.teamproject2k.connect.presentation.ui.chat.chat_list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.teamproject2k.connect.domain.models.ChatMetaDataBean
import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.use_case.chat.AddChatMessagesListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.chat.AddChatMetaDataListToLocalUseCase
import com.teamproject2k.connect.domain.use_case.chat.DeleteAllChatsFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.chat.GetChatListFromRemoteUseCase
import com.teamproject2k.connect.domain.use_case.chat.GetUserWithLastMessageWithUnreadCountFromLocalUseCase
import com.teamproject2k.connect.domain.use_case.user.AddUserListToLocalUseCase
import com.teamproject2k.connect.domain.utils.DomainFunctionHelper
import com.teamproject2k.connect.presentation.base.BaseViewModel
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
    private val getUserWithLastMessageWithUnreadCountFromLocalUseCase: GetUserWithLastMessageWithUnreadCountFromLocalUseCase,
    private val deleteAllChatsFromLocalUseCase: DeleteAllChatsFromLocalUseCase

) : BaseViewModel() {

    lateinit var loggedInUserDetails: UserBean

    private var isChatListFetched: Boolean = false
    var isDetailsInitialized = false

    val snackBarMessageState = mutableStateOf("")

    private val _getChatListStateFlow: MutableStateFlow<ResponseState<MutableList<ChatWithUserAndCountBean>>> =
        MutableStateFlow(ResponseState.none())
    val getChatListStateFlow = _getChatListStateFlow.asStateFlow()

    /**
     * Initializes the details of the logged-in user.
     * @param loggedInUserDetails The details of the logged-in user.
     */
    fun initializeData(loggedInUserDetails: UserBean) {
        this.loggedInUserDetails = loggedInUserDetails
        isDetailsInitialized = true
    }

    /**
     * Retrieves the list of chats for the logged-in user.
     * @param isForceUpdate Flag indicating whether to force update the chat list.
     * @param isNetworkAvailable Flag indicating whether the network is available.
     */
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
                            deleteAllChatsFromLocalUseCase()
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