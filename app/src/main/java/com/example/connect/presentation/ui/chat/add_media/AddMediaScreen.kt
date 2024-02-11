package com.example.connect.presentation.ui.chat.add_media

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.example.connect.presentation.ui.chat.chat_details.RepliedOnUI
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.GetPlayerView
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph
@Destination
@Composable
fun AddMediaScreen(
    navigator: DestinationsNavigator,
    message: String,
    mediaData: MediaData,
    loggedInUser: UsersBean,
    otherUsersBean: UsersBean,
    repliedOnChatMedia: ChatBean?
) {
    val viewModel: AddMediaViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(message, loggedInUser, otherUsersBean)
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }, topBar = {
        AppTopAppBar(
            showNavigationIcon = true,
            title = stringResource(R.string.upload_media),
            onNavigationIconClick = {
                navigator.popBackStack()
            }
        )
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            MediaSection(
                context = context,
                mediaData,
                modifier = Modifier.weight(1f)
            )
            AddMediaBottomSection(viewModel = viewModel, repliedOnChatMedia) {}
        }
    }
    //HandleSendMessageState(viewModel = viewModel)
    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
}

@Composable
private fun MediaSection(
    context: Context,
    selectedMedia: MediaData,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_IMAGE) {
            AsyncImage(
                model = selectedMedia.uri,
                contentDescription = stringResource(R.string.story_image),
                contentScale = ContentScale.Crop,
            )
        } else if (selectedMedia.mediaType == ConstantsHelper.MEDIA_TYPE_VIDEO) {
            GetPlayerView(
                context = context,
                uri = selectedMedia.uri.toString()
            ) { _, _ ->
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddMediaBottomSection(
    viewModel: AddMediaViewModel, repliedOnChatMedia: ChatBean?, onMediaPickRequest: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = if (repliedOnChatMedia == null) Alignment.CenterVertically else Alignment.Bottom
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        Surface(
            tonalElevation = 6.dp, modifier = Modifier
                .weight(1f)
                .clip(
                    if (repliedOnChatMedia == null) RoundedCornerShape(32.dp) else RoundedCornerShape(
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                        topStart = 6.dp,
                        topEnd = 6.dp
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (repliedOnChatMedia != null) {
                    RepliedOnUI(
                        modifier = Modifier.fillMaxWidth(),
                        message = repliedOnChatMedia,
                        loggedInUserFirebaseId = viewModel.loggedInUser.firebaseUserId,
                        otherUserName = viewModel.otherUser.name,
                        showCancelIconButton = false,
                    ) {
                    }
                }
                BasicTextField(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = viewModel.messageState.value,
                    onValueChange = { text -> viewModel.messageState.value = text },
                    decorationBox = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.messageState.value.isBlank()) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.message),
                                    color = ColorsHelper.gray(),
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = viewModel.messageState.value,
                                    color = ColorsHelper.black(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                )
            }
        }
        SpacerWidth8()
        if (!viewModel.isMessageSendingState.value) {
            IconButton(
                enabled = viewModel.messageState.value.isNotBlank(),
                onClick = {
                    if (viewModel.messageState.value.isNotBlank()) {
                        keyboardController?.hide()
                        if (context.isNetworkAvailable()) {
                            viewModel.sendMessage()
                        } else {
                            viewModel.snackBarMessageState.value =
                                context.getString(R.string.no_internet_connection)
                            FunctionHelper.vibrateDevice(context)
                        }
                    }
                }, colors = IconButtonDefaults.iconButtonColors(
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
fun HandleSendMessageState(viewModel: AddMediaViewModel, navigator: DestinationsNavigator) {

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
                    ScreenNameEnum.AddMediaScreen.name,
                    sendMessageState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            viewModel.isMessageSendingState.value = false
            viewModel.messageState.value = ""
            //  viewModel.repliedOnChatState.value = null
            navigator.popBackStack()
        }

        RequestStatusEnum.None -> {
            // No need to handle this
        }
    }
}