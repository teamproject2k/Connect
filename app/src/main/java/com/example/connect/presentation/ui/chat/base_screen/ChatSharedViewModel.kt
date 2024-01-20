package com.example.connect.presentation.ui.chat.base_screen

import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatSharedViewModel @Inject constructor() :
    BaseViewModel() {
    lateinit var usersDetails: UsersBean

    fun setCurrentUser(user: UsersBean) {
        usersDetails = user
    }
}