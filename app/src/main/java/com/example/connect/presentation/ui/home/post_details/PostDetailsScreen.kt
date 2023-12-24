package com.example.connect.presentation.ui.home.post_details

import androidx.compose.runtime.Composable
import com.example.connect.domain.models.PostBean
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@HomeNavGraph
@Destination
@Composable
fun PostDetailsScreen(navigator: DestinationsNavigator, postBean: PostBean) {
}