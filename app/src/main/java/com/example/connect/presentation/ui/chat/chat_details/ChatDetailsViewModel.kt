package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.useCase.chat.SendMessageToRemoteUseCase
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(private val sendMessageToRemoteUseCase: SendMessageToRemoteUseCase) :
    BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")

    init {
        sendMessage()
    }

    fun sendMessage() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val a = ChatBean(
                    firebaseId = "",
                    senderId = "OUbbPgo1mnUxIog93aqiFsaN07M2",
                    receiverId = "W7hF7ENqbbRP7c1Dc8StarkC1vF3",
                    "hello",
                    0L,
                    0L,
                    "",
                    "",
                    "",
                    ""
                )
                sendMessageToRemoteUseCase(a)
            }
        }
    }
}