package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor() : BaseViewModel() {

    val snackBarMessageState = mutableStateOf("")
    val messageState = mutableStateOf("")

    fun sendMessage() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

            }
        }
    }
}