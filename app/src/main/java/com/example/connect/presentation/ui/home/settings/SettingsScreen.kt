package com.example.connect.presentation.ui.home.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SettingsScreen() {
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    Scaffold(topBar = {
        AppTopAppBar(title = stringResource(id = R.string.settings))
    }) {
        Column(modifier = Modifier.padding(it)) {
        }
    }
}
