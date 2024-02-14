package com.example.connect.presentation.ui.chat.chat_details

import android.Manifest
import android.app.Activity.RESULT_OK
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.connect.R
import com.example.connect.domain.enums.MediaStateChangeEnum
import com.example.connect.domain.enums.MessageDeleteStatusEnum
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.ChatBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.ChatBottomSection
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.RepliedOnUI
import com.example.connect.presentation.ui.common.SpacerHeight16
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.TextBold16
import com.example.connect.presentation.ui.common.mediaPicker
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.destinations.AddMediaScreenDestination
import com.example.connect.presentation.ui.destinations.ShowMediaScreenDestination
import com.example.connect.presentation.ui.enums.MediaTypeEnum
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getIntentForSpeech
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@ChatNavGraph
@Destination
@Composable
fun ChatDetailsScreen(
    navigator: DestinationsNavigator, loggedInUser: UsersBean, otherUserDetails: UsersBean
) {
    val viewModel: ChatDetailsViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(loggedInUser, otherUserDetails)
    }

    val mediaPickerLauncher = mediaPicker {
        val mediaType = FunctionHelper.getMediaType(context.contentResolver, uri = it)
        if (mediaType != null) {
            navigator.navigate(
                AddMediaScreenDestination(
                    viewModel.messageState.value,
                    MediaData(it, mediaType),
                    viewModel.loggedInUser,
                    viewModel.otherUser,
                    viewModel.repliedOnChatState.value
                )
            )
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val text =
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (text != null) {
                viewModel.messageState.value = text
            }
        }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            if (it) {
                speechRecognizerLauncher.launch(getIntentForSpeech(context))
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.audio_permission_not_granted_please_grant_it_from_settings)
            }
        }

    if (!context.isNetworkAvailable()) {
        viewModel.snackBarMessageState.value = stringResource(id = R.string.no_internet_connection)
        FunctionHelper.vibrateDevice(context)
        navigator.popBackStack()
    }
    if (viewModel.listener == null) {
        viewModel.liveObserveChat()
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ChatDetailsTopSection(viewModel, navigator)
                ChatListSection(viewModel, loggedInUser.firebaseUserId, navigator)
            }
            ChatBottomSection(
                messageState = viewModel.messageState,
                mediaDataState = mutableStateOf(null),
                messageSendingState = viewModel.isMessageSendingState,
                loggedInUserFirebaseId = viewModel.loggedInUser.firebaseUserId,
                otherUserName = viewModel.otherUser.name,
                repliedOnChatBean = viewModel.repliedOnChatState.value,
                onRemoveRepliedOnChatRequest = { viewModel.repliedOnChatState.value = null },
                onSpeechRecognizerRequest = {
                    speechRecognizerLauncher.launch(getIntentForSpeech(context))
                },
                onAudioPermissionRequest = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onSendMessage = { viewModel.sendMessage() },
                onNoInternetError = {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                },
                showPickMediaIcon = true,
                onMediaPickRequest = {
                    mediaPickerLauncher.launch(PickVisualMediaRequest())
                }
            )
        }
        HandleSendMessageState(viewModel = viewModel)
        HandleDeleteMessageState(viewModel = viewModel)
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
fun ChatListSection(
    viewModel: ChatDetailsViewModel,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(state = lazyListState) {
        items(viewModel.chatListState, key = { it.firebaseId }) { chat ->
            ChatBubble(
                viewModel,
                chat,
                loggedInUserFirebaseId,
                otherUserName = viewModel.otherUser.name,
                navigator
            )
        }
        if (viewModel.chatListState.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.scrollToItem(viewModel.chatListState.lastIndex)
            }
        }
    }
}

@Composable
private fun ChatDetailsTopSection(
    viewModel: ChatDetailsViewModel, navigator: DestinationsNavigator
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
            model = viewModel.otherUser.profilePhoto,
            contentDescription = viewModel.otherUser.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        SpacerWidth16()
        Column {
            TextBold16(text = viewModel.otherUser.name, color = MaterialTheme.colorScheme.onPrimary)
            Text(
                text = viewModel.loggedInUser.connectUserId,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
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
            viewModel.repliedOnChatState.value = null
        }

        RequestStatusEnum.None -> {
            // No need to handle this
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBubble(
    viewModel: ChatDetailsViewModel,
    message: ChatBean,
    loggedInUserFirebaseId: String,
    otherUserName: String,
    navigator: DestinationsNavigator
) {
    val isMessageFromLoggedInUser = message.senderId == loggedInUserFirebaseId
    var showDeleteMessageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val translateX = mutableFloatStateOf(0.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = if (isMessageFromLoggedInUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(.55f),
            contentAlignment = if (isMessageFromLoggedInUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            val baseModifier = Modifier
                .clip(
                    if (isMessageFromLoggedInUser) RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp
                    ) else RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(
                    if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.primary.copy(.8f) else ColorsHelper.chatBubbleOtherUserBg()
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            FunctionHelper.vibrateDevice(context, 100)
                            showDeleteMessageDialog = true
                        },
                        onTap = {
                            val mediaType =
                                if (message.mediaType == MediaTypeEnum.Image.name || message.mediaType == MediaTypeEnum.TextImage.name) ConstantsHelper.MEDIA_TYPE_IMAGE else if (message.mediaType == MediaTypeEnum.Video.name || message.mediaType == MediaTypeEnum.TextVideo.name) ConstantsHelper.MEDIA_TYPE_VIDEO else ""
                            if (mediaType.isNotBlank()) {
                                navigator.navigate(
                                    ShowMediaScreenDestination(
                                        MediaData(message.mediaUrl.toUri(), mediaType)
                                    )
                                )
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount > 5) {
                                // right swipe
                                translateX.value = dragAmount
                                viewModel.repliedOnChatState.value = message
                            }
                        },
                        onDragEnd = {
                            if (translateX.value != 0.0f) {
                                FunctionHelper.vibrateDevice(context, 100)
                                translateX.value = 0.0f
                            }
                        }
                    )
                }
                .graphicsLayer {
                    translationX = translateX.value
                }
                .padding(horizontal = 6.dp, vertical = 4.dp)
            Column(
                modifier = if (message.mediaUrl.isNotBlank()) baseModifier
                    .fillMaxWidth()
                    .aspectRatio(1f) else baseModifier
            ) {
                if (message.repliedOnChatId != null) {
                    val repliedOnMessage =
                        viewModel.chatListState.find { it.firebaseId == message.repliedOnChatId }
                    if (repliedOnMessage != null) {
                        RepliedOnUI(
                            message = repliedOnMessage,
                            loggedInUserFirebaseId = viewModel.loggedInUser.firebaseUserId,
                            senderNameColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black(),
                            dividerColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.primary else ColorsHelper.gray(),
                            messageColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black(),
                            otherUserName = otherUserName,
                            showCancelIconButton = false
                        )
                    }
                }
                if (message.mediaUrl.isNotBlank()) {
                    HandleMessageMediaSection(
                        mediaUrl = message.mediaUrl,
                        mediaType = message.mediaType
                    )
                }
                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        color = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black()
                    )
                }
            }
        }
    }
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 8.dp)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onLongPress = {
//                        FunctionHelper.vibrateDevice(context, 100)
//                        showDeleteMessageDialog = true
//                    }
//                )
//            }
//            .pointerInput(Unit) {
//                detectHorizontalDragGestures(
//                    onHorizontalDrag = { change, dragAmount ->
//                        change.consume()
//                        if (dragAmount > 5) {
//                            // right swipe
//                            translateX.value = dragAmount
//                            viewModel.repliedOnChatState.value = message
//                        }
//                    },
//                    onDragEnd = {
//                        if (translateX.value != 0.0f) {
//                            FunctionHelper.vibrateDevice(context, 100)
//                            translateX.value = 0.0f
//                        }
//                    }
//                )
//            }
//            .graphicsLayer {
//                translationX = translateX.value
//            },
//        horizontalAlignment = if (isMessageFromLoggedInUser) Alignment.End else Alignment.Start,
//    ) {
//        Column(
//            modifier = Modifier
//                .clip(
//                    if (isMessageFromLoggedInUser) RoundedCornerShape(
//                        topStart = 12.dp,
//                        topEnd = 12.dp,
//                        bottomStart = 12.dp
//                    ) else RoundedCornerShape(
//                        topStart = 12.dp,
//                        topEnd = 12.dp,
//                        bottomEnd = 12.dp
//                    )
//                )
//                .background(
//                    if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.primary.copy(.8f) else ColorsHelper.chatBubbleOtherUserBg()
//                )
//                .padding(2.dp)
//        ) {
//            if (message.mediaUrl.isNotBlank()) {
//                if (message.mediaType == MediaTypeEnum.Image.name || message.mediaType == MediaTypeEnum.TextImage.name) {
//                    if (isImageLoadingError) {
//                        Box(
//                            modifier = Modifier
//                                .size(100.dp)
//                                .background(ColorsHelper.lightGray()),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(text = stringResource(R.string.couldn_t_load_image))
//                        }
//                    } else {
//                        AsyncImage(
//                            model = message.mediaUrl,
//                            contentDescription = stringResource(R.string.story_image),
//                            modifier = Modifier
//                                .size(100.dp)
//                                .clickable {
//                                    val mediaType =
//                                        if (message.mediaType == MediaTypeEnum.Image.name || message.mediaType == MediaTypeEnum.TextImage.name) ConstantsHelper.MEDIA_TYPE_IMAGE else if (message.mediaType == MediaTypeEnum.Video.name || message.mediaType == MediaTypeEnum.TextVideo.name) ConstantsHelper.MEDIA_TYPE_VIDEO else ""
//                                    if (mediaType.isNotBlank()) {
//                                        navigator.navigate(
//                                            ShowMediaScreenDestination(
//                                                MediaData(message.mediaUrl.toUri(), mediaType)
//                                            )
//                                        )
//                                    }
//                                },
//                            contentScale = ContentScale.Crop,
//                            onLoading = {
//                                isImageLoadingError = false
//                            },
//                            onSuccess = {
//                                isImageLoadingError = false
//                            },
//                            onError = {
//                                isImageLoadingError = true
//                            }
//                        )
//                    }
//                } else {
//                    AsyncImage(
//                        model = message.mediaUrl,
//                        contentDescription = stringResource(R.string.story_image),
//                        modifier = Modifier
//                            .size(100.dp)
//                            .clickable {
//                                val mediaType =
//                                    if (message.mediaType == MediaTypeEnum.Image.name || message.mediaType == MediaTypeEnum.TextImage.name) ConstantsHelper.MEDIA_TYPE_IMAGE else if (message.mediaType == MediaTypeEnum.Video.name || message.mediaType == MediaTypeEnum.TextVideo.name) ConstantsHelper.MEDIA_TYPE_VIDEO else ""
//                                if (mediaType.isNotBlank()) {
//                                    navigator.navigate(
//                                        ShowMediaScreenDestination(
//                                            MediaData(message.mediaUrl.toUri(), mediaType)
//                                        )
//                                    )
//                                }
//                            },
//                        contentScale = ContentScale.Crop,
//                        onLoading = {
//                            isImageLoadingError = false
//                        },
//                        onSuccess = {
//                            isImageLoadingError = false
//                        },
//                        onError = {
//                            isImageLoadingError = true
//                        }
//                    )
//                }
//            }
//            val repliedOnMessage =
//                viewModel.chatListState.find { it.firebaseId == message.repliedOnChatId }
//            if (message.repliedOnChatId != null && repliedOnMessage != null) {
//                Column {
//                    RepliedOnUI(
//                        message = repliedOnMessage,
//                        loggedInUserFirebaseId = viewModel.loggedInUser.firebaseUserId,
//                        senderNameColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black(),
//                        dividerColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.primary else ColorsHelper.gray(),
//                        messageColor = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black(),
//                        otherUserName = otherUserName,
//                        showCancelIconButton = false
//                    )
//                }
//            }
//            Text(
//                text = message.message,
//                fontWeight = FontWeight.Normal,
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(8.dp),
//                color = if (isMessageFromLoggedInUser) MaterialTheme.colorScheme.onPrimary else ColorsHelper.black()
//            )
//        }
//        SpacerHeight2()
//        Text(
//            text = FunctionHelper.getFormattedDateTime(message.sentAt, "dd/MM/yy, hh:mm a"),
//            color = ColorsHelper.gray(),
//            fontWeight = FontWeight.Medium,
//            fontSize = 11.sp,
//        )
//    }

    if (showDeleteMessageDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteMessageDialog = false
            }
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Start),
                        text = stringResource(R.string.delete_message),
                        fontSize = 14.sp,
                        color = ColorsHelper.black()
                    )
                    SpacerHeight16()
                    if (isMessageFromLoggedInUser) {
                        TextButton(
                            onClick = {
                                if (context.isNetworkAvailable()) {
                                    viewModel.deleteMessage(
                                        MessageDeleteStatusEnum.DeletedForEveryone.name,
                                        message
                                    )
                                } else {
                                    viewModel.snackBarMessageState.value =
                                        context.getString(R.string.no_internet_connection)
                                    FunctionHelper.vibrateDevice(context)
                                }
                                showDeleteMessageDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.delete_for_everyone))
                        }
                    }
                    TextButton(
                        onClick = {
                            if (context.isNetworkAvailable()) {
                                val deleteFor = when {
                                    message.deletedBy != MessageDeleteStatusEnum.DeletedForNone.name -> {
                                        MessageDeleteStatusEnum.DeletedForEveryone.name
                                    }

                                    isMessageFromLoggedInUser -> {
                                        MessageDeleteStatusEnum.DeletedForSender.name
                                    }

                                    else -> {
                                        MessageDeleteStatusEnum.DeletedForReceiver.name
                                    }
                                }
                                viewModel.deleteMessage(deleteFor, message)
                            } else {
                                viewModel.snackBarMessageState.value =
                                    context.getString(R.string.no_internet_connection)
                                FunctionHelper.vibrateDevice(context)
                            }
                            showDeleteMessageDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.delete_for_me))
                    }
                    TextButton(
                        onClick = {
                            showDeleteMessageDialog = false
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun HandleMessageMediaSection(mediaUrl: String, mediaType: String) {
    var mediaLoadingState by remember {
        mutableStateOf("")
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = mediaUrl,
            imageLoader = ImageLoader.Builder(LocalContext.current)
                .components {
                    if (mediaType == MediaTypeEnum.Video.name || mediaType == MediaTypeEnum.TextVideo.name) {
                        add(VideoFrameDecoder.Factory())
                    }
                }
                .build(),
            modifier = when (mediaLoadingState) {
                MediaStateChangeEnum.Loading.name -> Modifier.shimmer()
                MediaStateChangeEnum.Error.name -> Modifier.background(
                    ColorsHelper.lightGray()
                )

                else -> Modifier
            },
            contentDescription = null,
            onLoading = {
                mediaLoadingState = MediaStateChangeEnum.Loading.name
            }, onSuccess = {
                mediaLoadingState = MediaStateChangeEnum.Success.name
            },
            onError = {
                mediaLoadingState = MediaStateChangeEnum.Error.name
            },
            contentScale = ContentScale.Crop
        )
        if (mediaLoadingState == MediaStateChangeEnum.Error.name) {
            Text(
                text = stringResource(R.string.couldn_t_load_media),
                fontSize = 14.sp
            )
        }
        if (mediaType == MediaTypeEnum.Video.name || mediaType == MediaTypeEnum.TextVideo.name) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = stringResource(
                    id = R.string.play_video
                )
            )
        }
    }
}

@Composable
fun HandleDeleteMessageState(viewModel: ChatDetailsViewModel) {
    val updateMessageState = viewModel.deleteMessageStateFlow.collectAsState().value

    var isExceptionHandled by remember {
        mutableStateOf(false)
    }

    when (updateMessageState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(loadingText = stringResource(R.string.deleting_message))
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    updateMessageState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.ChatDetailsScreen.name,
                    updateMessageState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {

        }

        RequestStatusEnum.None -> {
            // No need to handle this
        }
    }
}


