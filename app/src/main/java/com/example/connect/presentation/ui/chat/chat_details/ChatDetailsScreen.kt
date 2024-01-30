package com.example.connect.presentation.ui.chat.chat_details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.SpacerWidth16
import com.example.connect.presentation.ui.common.TextBold16
import com.example.connect.presentation.ui.common.TransparentTextField
import com.example.connect.presentation.utils.ChatNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@ChatNavGraph
@Destination
@Composable
fun ChatDetailsScreen(navigator: DestinationsNavigator, loggedInUserDetails: UsersBean) {
    val viewModel: ChatDetailsViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ChatDetailsTopSection(
                    loggedInUserDetails,
                    loggedInUserDetails,
                    navigator
                )
            }
            ChatBubble(message = Message("hehehe", isUser = true))
            ChatDetailsBottomSection(
                viewModel,
                loggedInUserDetails,
                loggedInUserDetails
            )
        }
        LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
            if (viewModel.snackBarMessageState.value.isNotBlank()) {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                    viewModel.snackBarMessageState.value = ""
                }
            }
        }
    }
}

@Composable
private fun ChatDetailsTopSection(
    loggedInUserDetails: UsersBean,
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
            Text(text = "Online", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
fun ChatDetailsBottomSection(
    viewModel: ChatDetailsViewModel,
    loggedInUserDetails: UsersBean,
    otherUserDetails: UsersBean
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransparentTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    border = BorderStroke(1.dp, ColorsHelper.black()),
                    shape = RoundedCornerShape(32.dp)
                ),
            value = viewModel.messageState.value,
            onValueChange = { text -> viewModel.messageState.value = text },
            placeholder = {
                Text(
                    stringResource(id = R.string.message),
                    color = ColorsHelper.gray()
                )
            }
        )
        IconButton(onClick = {
            if (viewModel.messageState.value.isNotBlank()) {
                viewModel.sendMessage()
            }
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = stringResource(R.string.post_comment)
            )
        }
    }
}

data class Message(val text: String, val isUser: Boolean)

@Composable
fun ChatBubble(message: Message) {
    val backgroundColor = if (message.isUser) {
        Color(0xFFDCF8C6)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(2.dp)
        ) {
            Text(
                text = message.text,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


class TriangleEdgeShape(val offset: Int) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = Path().apply {
            moveTo(x = 0f, y = size.height - offset)
            lineTo(x = 0f, y = size.height)
            lineTo(x = 0f + offset, y = size.height)
        }
        return Outline.Generic(path = trianglePath)
    }
}

