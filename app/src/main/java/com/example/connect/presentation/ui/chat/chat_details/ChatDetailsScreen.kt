package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.SpacerHeight2
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TextBold16
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@ChatNavGraph
@Destination
@Composable
fun ChatDetailsScreen(
    navigator: DestinationsNavigator,
    loggedInUserFirebaseId: String,
    otherUserDetails: UsersBean
) {
    val viewModel: ChatDetailsViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    if (viewModel.listener == null) {
        viewModel.liveObserveChat(loggedInUserFirebaseId, otherUserDetails.firebaseUserId)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(
                    ColorsHelper
                        .lightGray()
                        .copy(alpha = 0.05f)
                ), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                ChatDetailsTopSection(
                    otherUserDetails,
                    navigator
                )
                ChatListSection(viewModel, loggedInUserFirebaseId)
            }
            ChatDetailsBottomSection(
                viewModel,
                loggedInUserFirebaseId,
                otherUserDetails
            )
        }
        HandleSendMessageState(viewModel = viewModel)
        LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
            if (viewModel.snackBarMessageState.value.isNotBlank()) {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                    viewModel.snackBarMessageState.value = ""
                }
            }
        }
        LaunchedEffect(key1 = viewModel.onListenerErrorOccurredState.value) {
            if (viewModel.onListenerErrorOccurredState.value.isNotBlank()) {
                context.showToast(viewModel.onListenerErrorOccurredState.value)
                navigator.popBackStack()
            }
        }
    }
}

@Composable
fun ChatListSection(viewModel: ChatDetailsViewModel, loggedInUserFirebaseId: String) {
    LazyColumn {
        items(viewModel.chatListState) { chat ->
            ChatBubble(chat, loggedInUserFirebaseId)
        }
    }
}

@Composable
private fun ChatDetailsTopSection(
    otherUserDetails: UsersBean,
    navigator: DestinationsNavigator
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navigator.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.go_back),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            model = otherUserDetails.profilePhoto,
            contentDescription = otherUserDetails.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        SpacerWidth16()
        Column {
            TextBold16(text = otherUserDetails.name, color = MaterialTheme.colorScheme.onPrimary)
            // TODO: 31/01/24 aryan handle online
            Text(text = "Online", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatDetailsBottomSection(
    viewModel: ChatDetailsViewModel,
    loggedInUserFirebaseId: String,
    otherUserDetails: UsersBean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    border = BorderStroke(
                        1.dp,
                        ColorsHelper
                            .black()
                            .copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            value = viewModel.messageState.value,
            onValueChange = { text -> viewModel.messageState.value = text },
            decorationBox = {
                if (viewModel.messageState.value.isBlank()) {
                    Text(
                        stringResource(id = R.string.message),
                        color = ColorsHelper.gray(),
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        viewModel.messageState.value,
                        color = ColorsHelper.black(),
                        fontSize = 14.sp
                    )
                }
            }
        )
        SpacerWidth8()
        if (!viewModel.isMessageSendingState.value) {
            IconButton(
                enabled = viewModel.messageState.value.isNotBlank(),
                onClick = {
                    if (viewModel.messageState.value.isNotBlank()) {
                        keyboardController?.hide()
                        if (context.isNetworkAvailable()) {
                            viewModel.sendMessage(
                                loggedInUserFirebaseId,
                                otherUserDetails.firebaseUserId
                            )
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = ColorsHelper.gray().copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    modifier = Modifier.padding(10.dp),
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = stringResource(R.string.post_comment),
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                strokeWidth = 1.5.dp
            )
        }
    }
}

@Composable
fun HandleSendMessageState(viewModel: ChatDetailsViewModel) {

    val sendMessageState = viewModel.sendMessageStateFlow.collectAsState().value

    var isExceptionHandled by remember {
        mutableStateOf(false)
    }

    when (sendMessageState.status) {
        RequestStatusEnum.Loading -> {
            viewModel.isMessageSendingState.value = true
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.isMessageSendingState.value = false
                viewModel.snackBarMessageState.value =
                    sendMessageState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.ChatDetailsScreen.name,
                    sendMessageState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            viewModel.isMessageSendingState.value = false
            viewModel.messageState.value = ""
        }

        RequestStatusEnum.None -> {
            // No need to handle this
        }
    }
}

@Composable
fun ChatBubble(message: ChatBean, loggedInUserFirebaseId: String) {
    val isMessageFromLoggedInUser = message.senderId == loggedInUserFirebaseId
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = if (isMessageFromLoggedInUser) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary)
                .padding(2.dp)
        ) {
            Text(
                text = message.message,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                color = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black()
            )
        }
        SpacerHeight2()
        Text(
            text = FunctionHelper.getFormattedDateTime(message.sentAt, "dd-MM-yy, hh:mm a"),
            color = ColorsHelper.gray(),
            fontSize = 11.sp,
        )
    }
}


