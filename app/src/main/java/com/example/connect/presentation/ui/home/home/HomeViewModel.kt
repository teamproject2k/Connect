package com.example.connect.presentation.ui.home.home

import androidx.compose.runtime.mutableStateOf
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel() {
    private val _postDetailsStateFlow: MutableStateFlow<ResponseState<Nothing>> =
        MutableStateFlow(ResponseState.none())

    val postDetailsStateFlow: StateFlow<ResponseState<Nothing>> get() = _postDetailsStateFlow

    val snackBarMessageState = mutableStateOf("")

}