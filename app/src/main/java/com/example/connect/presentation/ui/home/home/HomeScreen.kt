package com.example.connect.presentation.ui.home.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.presentation.ui.destinations.SearchScreenDestination
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    Scaffold(topBar = {
        Surface(shadowElevation = 3.dp) {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }, actions = {
                IconButton(onClick = { navigator.navigate(SearchScreenDestination.route) }) {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                }
            })
        }
    }) {
        Column(modifier = Modifier.padding(it)) {

        }
    }
}