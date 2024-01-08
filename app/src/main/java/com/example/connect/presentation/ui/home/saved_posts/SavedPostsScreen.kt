package com.example.connect.presentation.ui.home.saved_posts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.models.PostBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.PostListLoadingSection
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SavedPostsScreen(navigator: DestinationsNavigator) {
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: SavedPostsViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        AppTopAppBar(title = stringResource(R.string.saved_posts))
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleGetSavedPostsState(viewModel, navigator)
        }
    }

    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.getSavedPosts(homeSharedViewModel.usersDetails.savedPosts)
    }
}

@Composable
fun HandleGetSavedPostsState(viewModel: SavedPostsViewModel, navigator: DestinationsNavigator) {
    val savedPostsState = viewModel.getSavedPostsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (savedPostsState.status) {
        RequestStatusEnum.Loading -> {
            PostListLoadingSection()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    savedPostsState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SavedPostsScreen.name,
                    savedPostsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            DisplayPostsList(navigator, savedPostsState.data ?: emptyList())
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun DisplayPostsList(navigator: DestinationsNavigator, postBeans: List<PostBean>) {

}
