package com.teamproject2k.connect.presentation.ui.chat.add_media

import android.Manifest
import android.app.Activity
import android.content.Context
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.models.ChatBean
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.network_request_response.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.AppTopAppBar
import com.teamproject2k.connect.presentation.ui.common.ChatBottomSection
import com.teamproject2k.connect.presentation.ui.common.GetPlayerView
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ChatNavGraph
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
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
        viewModel.initializeData(message, loggedInUser, otherUsersBean, repliedOnChatMedia)
    }
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
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
                speechRecognizerLauncher.launch(FunctionHelper.getIntentForSpeech(context))
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.audio_permission_not_granted_please_grant_it_from_settings)
            }
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
            ChatBottomSection(
                messageState = viewModel.messageState,
                mediaDataState = mutableStateOf(mediaData),
                messageSendingState = viewModel.isMessageSendingState,
                loggedInUserFirebaseId = viewModel.loggedInUser.firebaseUserId,
                otherUserName = viewModel.otherUser.name,
                repliedOnChatBean = viewModel.repliedOnChatMediaState.value,
                onRemoveRepliedOnChatRequest = { viewModel.repliedOnChatMediaState.value = null },
                onSpeechRecognizerRequest = {
                    speechRecognizerLauncher.launch(FunctionHelper.getIntentForSpeech(context))
                },
                onAudioPermissionRequest = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onSendMessage = { viewModel.sendMessage(mediaData) },
                onNoInternetError = {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                },
                showPickMediaIcon = false
            )
        }
    }
    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
    HandleSendMessageState(viewModel = viewModel, navigator = navigator)
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
            viewModel.repliedOnChatMediaState.value = null
            navigator.popBackStack()
        }

        RequestStatusEnum.None -> {
            // No need to handle this
        }
    }
}