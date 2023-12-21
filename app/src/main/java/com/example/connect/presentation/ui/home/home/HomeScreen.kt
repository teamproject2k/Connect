package com.example.connect.presentation.ui.home.home

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.presentation.ui.chat.base_screen.ChatActivity
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostItemLayout
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)

    Scaffold(topBar = {
        AppTopAppBar(title = stringResource(id = R.string.app_name), actions = {
            IconButton(onClick = {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("userDetails", homeSharedViewModel.usersDetails)
                activity.startActivity(intent)
            }) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = stringResource(id = R.string.chat)
                )
            }
        })
    }) {
        Column(modifier = Modifier.padding(it)) {
            PostItemLayout(usersDetails = homeSharedViewModel.usersDetails)
        }
    }
}