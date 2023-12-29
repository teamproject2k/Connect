package com.example.connect.presentation.ui.home.add_story

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun AddStoryScreen(navigator: DestinationsNavigator) {
    val viewModel: AddStoryViewModel = hiltViewModel()
    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackBarHostState = SnackbarHostState()

    Scaffold(topBar = {
        Surface(shadowElevation = 3.dp) {
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.add_story),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }, actions = {
                Button(
                    // enabled = viewModel.captionTextState.value.isNotBlank() || viewModel.selectedMediaState.value != null,
                    onClick = {
//                        handleButtonClick(
//                            viewModel,
//                            context,
//                            sharedViewModel.usersDetails.firebaseUserId
//                        )
                    }
                ) {
                    Text(text = stringResource(R.string.post))
                }
            })
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HandleAddStorySection(viewModel, context, navigator)
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
}

@Composable
private fun HandleAddStorySection(
    viewModel: AddStoryViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val addStoryState = viewModel.uploadStoryStateFlow.collectAsState().value
    when (addStoryState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.uploading_story))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.story_uploaded_successfully))
                navigator.popBackStack()
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                if (addStoryState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    viewModel.snackBarMessageState.value =
                        addStoryState.message ?: stringResource(id = R.string.some_error_occurred)
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "AddStoryScreen",
                    addStoryState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle it
        }
    }
}