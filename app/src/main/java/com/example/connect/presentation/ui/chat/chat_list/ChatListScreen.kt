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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.chat.base_screen.ChatSharedViewModel
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.TextBold16
import com.example.connect.presentation.utils.ChatNavGraph
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@ChatNavGraph(start = true)
@Destination
@Composable
fun ChatListScreen() {
    val chatSharedViewModel: ChatSharedViewModel = hiltViewModel(LocalActivity.current)
    Scaffold(topBar = {
        Surface(shadowElevation = 3.dp) {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.chats),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            })
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            ChatList(chatSharedViewModel.usersDetails)
        }
    }
}

@Composable
fun ChatList(userDetails: UsersBean) {
    LazyColumn {
        items(12) {
            ChatListItem(userDetails = userDetails) {
                // navigate to chat details screen
            }
        }
    }
}

@Composable
fun ChatListItem(modifier: Modifier = Modifier, userDetails: UsersBean, onItemClick: () -> (Unit)) {
    Column(modifier = Modifier.clickable {
        onItemClick()
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ConstraintLayout {
                val (profileImageRef, activityStatusRef) = createRefs()
                AsyncImage(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .constrainAs(profileImageRef) {},
                    model = userDetails.profilePhoto,
                    contentDescription = userDetails.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_default_user)
                )
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp, bottom = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.inversePrimary)
                        .constrainAs(activityStatusRef) {
                            top.linkTo(profileImageRef.bottom)
                            bottom.linkTo(profileImageRef.bottom)
                            end.linkTo(profileImageRef.end)
                        }
                ) {}
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                TextBold16(text = userDetails.name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hello. How are you?",
                        fontSize = 13.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = "·",
                        fontSize = 20.sp,
                        color = ColorsHelper.gray(),
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "12 min", fontSize = 12.sp, color = ColorsHelper.gray())
                }
            }
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "3", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        DividerLightGrayAlpha50()
    }
}
