package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserListLoading() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(10) {
            UsersListItemLoading()
        }
    }
}

@Composable
fun UsersListItemLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        UserDetailsSectionLoading(modifier = Modifier.padding(16.dp))
        Divider()
    }
}