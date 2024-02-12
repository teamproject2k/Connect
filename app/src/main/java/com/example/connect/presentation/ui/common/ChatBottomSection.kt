package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.domain.models.ChatBean
import com.example.connect.presentation.ui.models.MediaData
import com.example.connect.presentation.utils.FunctionHelper.checkAudioPermissionGranted
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatBottomSection(
    messageState: MutableState<String>,
    mediaDataState: MutableState<MediaData?>,
    messageSendingState: MutableState<Boolean>,
    loggedInUserFirebaseId: String,
    otherUserName: String,
    repliedOnChatBean: ChatBean?,
    onRemoveRepliedOnChatRequest: () -> Unit,
    onSpeechRecognizerRequest: () -> Unit,
    onAudioPermissionRequest: () -> Unit,
    onSendMessage: () -> Unit,
    onNoInternetError: () -> Unit,
    showPickMediaIcon: Boolean,
    onMediaPickRequest: () -> Unit = {},
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = if (repliedOnChatBean == null) Alignment.CenterVertically else Alignment.Bottom
    ) {
        Surface(
            tonalElevation = 6.dp,
            modifier = Modifier
                .weight(1f)
                .clip(
                    if (repliedOnChatBean == null) RoundedCornerShape(32.dp)
                    else RoundedCornerShape(
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                        topStart = 6.dp,
                        topEnd = 6.dp
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (repliedOnChatBean != null) {
                    RepliedOnUI(
                        modifier = Modifier.fillMaxWidth(),
                        message = repliedOnChatBean,
                        loggedInUserFirebaseId = loggedInUserFirebaseId,
                        otherUserName = otherUserName,
                        showCancelIconButton = true,
                    ) {
                        onRemoveRepliedOnChatRequest()
                    }
                }
                TransparentTextField(
                    value = messageState.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    onValueChange = { messageState.value = it },
                    placeholder = {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = R.string.message),
                            color = ColorsHelper.gray(),
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        if (showPickMediaIcon) {
                            IconButton(
                                onClick = { onMediaPickRequest() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .rotate(45f),
                                    imageVector = Icons.Default.Attachment,
                                    contentDescription = stringResource(id = R.string.add_media),
                                    tint = ColorsHelper.gray()
                                )
                            }
                        }
                    }
                )
            }
        }
        SpacerWidth8()
        if (!messageSendingState.value) {
            IconButton(
                onClick = {
                    if (messageState.value.isBlank() && mediaDataState.value == null) {
                        if (checkAudioPermissionGranted(context)) {
                            onSpeechRecognizerRequest()
                        } else {
                            onAudioPermissionRequest()
                        }
                    } else {
                        keyboardController?.hide()
                        if (context.isNetworkAvailable()) {
                            onSendMessage()
                        } else {
                            onNoInternetError()
                        }
                    }
                }, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = ColorsHelper.gray().copy(alpha = 0.6f)
                )
            ) {
                if (messageState.value.isBlank() && mediaDataState.value == null) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.mic),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                    )
                } else {
                    Icon(
                        modifier = Modifier.padding(10.dp),
                        painter = painterResource(id = R.drawable.ic_send),
                        contentDescription = stringResource(R.string.post_comment),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                    )
                }
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
