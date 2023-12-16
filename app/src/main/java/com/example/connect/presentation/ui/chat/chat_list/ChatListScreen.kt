package com.example.connect.presentation.ui.chat.chat_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.connect.presentation.utils.ChatNavGraph
import com.ramcosta.composedestinations.annotation.Destination


@ChatNavGraph(start = true)
@Destination
@Composable
fun ChatListScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Coming Soon")
    }
}