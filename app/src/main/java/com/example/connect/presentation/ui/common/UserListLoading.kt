package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserListLoading() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(10) {
            UsersListItemLoading()
        }
    }
}