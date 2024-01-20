package com.example.connect.presentation.ui.chat.chat_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.ChatWithUserDetails
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.chat.base_screen.ChatSharedViewModel
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight4
import com.example.connect.presentation.ui.destinations.ChatDetailsScreenDestination
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.utils.ChatNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph(start = true)
@Destination
@Composable
fun ChatListScreen(navigator: DestinationsNavigator) {
    val chatSharedViewModel: ChatSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: ChatListViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            AppTopAppBar(title = stringResource(id = R.string.chats))
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HandleChatListSectionState(chatSharedViewModel.usersDetails, viewModel, navigator)
        }
    }
    LaunchedEffect(viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
            viewModel.snackBarMessageState.value = ""
        }
    }
}

@Composable
fun HandleChatListSectionState(
    loggedInUser: UsersBean,
    viewModel: ChatListViewModel,
    navigator: DestinationsNavigator
) {
    val getChatListState = viewModel.getChatListStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (getChatListState.status) {
        RequestStatusEnum.Loading -> {
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    getChatListState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.ChatListScreen.name,
                    getChatListState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            ChatList(loggedInUser, getChatListState.data, navigator)
        }

        RequestStatusEnum.None -> {
            // do not handle this
        }
    }
}

@Composable
private fun ChatList(
    loggedInUser: UsersBean,
    chatList: MutableList<ChatWithUserDetails>?,
    navigator: DestinationsNavigator
) {
    if (chatList.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = stringResource(R.string.no_chats_found))
        }
    } else {
        LazyColumn {
            items(chatList) { chat->
                ChatListItem(otherUser = loggedInUser, chat) {
                    navigator.navigate(ChatDetailsScreenDestination())
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    modifier: Modifier = Modifier,
    otherUser: UsersBean,
    chat: ChatWithUserDetails,
    onItemClick: () -> (Unit)
) {
    Column(modifier = Modifier.clickable {
        onItemClick()
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(16.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                model = otherUser.profilePhoto,
                contentDescription = otherUser.name,
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_default_user)
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = otherUser.name,
                        fontWeight = FontWeight(500),
                        color = ColorsHelper.black(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = "12 min",
                        fontSize = 12.sp,
                        color = ColorsHelper.gray(),
                        fontWeight = FontWeight.Medium
                    )
                }
                SpacerHeight4()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Hello. How are you? Hello mat bolo gaendue prasad chamasssss",
                        fontSize = 13.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        DividerLightGrayAlpha50()
    }
}
