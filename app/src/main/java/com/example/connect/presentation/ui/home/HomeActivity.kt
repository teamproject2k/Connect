package com.example.connect.presentation.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.RequestStatusEnum
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.NavGraphs
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight18
import com.example.connect.presentation.ui.common.SpacerWidth18
import com.example.connect.presentation.ui.common.TextBold18
import com.example.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.example.connect.presentation.ui.common.getHeightToMaintainAspectRatio
import com.example.connect.presentation.ui.destinations.AddPostScreenDestination
import com.example.connect.presentation.ui.destinations.HomeScreenDestination
import com.example.connect.presentation.ui.destinations.UserProfileScreenDestination
import com.example.connect.presentation.ui.models.BottomAppBarItemData
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var viewModel: HomeSharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel = hiltViewModel()
            CompositionLocalProvider(LocalActivity provides this) {
                ConnectTheme {
                    HandleGetDeviceIdFlow()
                    HandleUserDetailsFlow()
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HandleGetDeviceIdFlow() {
        var showNewDeviceLoginAlertDialog by remember {
            mutableStateOf(false)
        }
        val getDeviceIdState = viewModel.deviceIdStateFlow.collectAsState().value
        when (getDeviceIdState.status) {
            RequestStatusEnum.LOADING -> {
                LoaderFullScreen()
            }

            RequestStatusEnum.EXCEPTION -> {
                when (getDeviceIdState.message) {
                    ErrorCodes.NoUserFound -> {
                        showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                        logout()
                    }
                    ErrorCodes.NewLogin -> {
                        showNewDeviceLoginAlertDialog = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            showNewDeviceLoginAlertDialog = false
                            logout()
                        }, ConstantsHelper.NewDeviceDialogDismissTime)
                    }
                    else -> {
                        showToast(
                            getDeviceIdState.message
                                ?: stringResource(id = R.string.something_went_wrong)
                        )
                    }
                }

            }

            RequestStatusEnum.SUCCESS -> {
                // no need to handle
            }

            RequestStatusEnum.NONE -> {
                // no need to handle
            }
        }
        if (showNewDeviceLoginAlertDialog) {
            AlertDialog(
                onDismissRequest = { showNewDeviceLoginAlertDialog = false },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp)
                ) {
                    TextBold18(text = stringResource(R.string.logging_you_out))
                    SpacerHeight18()
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        SpacerWidth18()
                        Text(text = stringResource(R.string.we_have_detected_your_account_logged_into_another_device))
                    }
                }
            }
        }
    }

    @Composable
    private fun HandleUserDetailsFlow() {
        val getUserDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
        when (getUserDetailsState.status) {
            RequestStatusEnum.LOADING -> {
                LoaderFullScreen(stringResource(R.string.getting_user_details))
            }

            RequestStatusEnum.SUCCESS -> {
                CreateUi()
            }

            RequestStatusEnum.EXCEPTION -> {
                if (getUserDetailsState.message == ErrorCodes.NoUserFound) {
                    logout()
                } else {
                    if (getUserDetailsState.message.isNullOrBlank()) {
                        showToast(stringResource(id = R.string.something_went_wrong))
                    } else {
                        showToast(getUserDetailsState.message)
                    }
                }
            }

            RequestStatusEnum.NONE -> {

            }
        }
    }


    @Composable
    private fun CreateUi() {
        val selectedRouteState = rememberSaveable {
            mutableStateOf(HomeScreenDestination.route)
        }
        val navController = rememberNavController()
        Scaffold(bottomBar = {
            Surface(tonalElevation = 4.dp) {
                NavigationBar(
                    modifier = Modifier
                        .height(
                            getHeightToMaintainAspectRatio(
                                noOfRows = 1, itemsRequiredPerRow = 6
                            )
                        )
                ) {
                    getBottomNavBarItemList().forEach { data ->
                        NavigationBarItem(
                            selected = selectedRouteState.value == data.routeName,
                            onClick = {
                                selectedRouteState.value = data.routeName
                                navController.navigate(
                                    data.routeName
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            icon = {
                                Image(
                                    imageVector = if (selectedRouteState.value == data.routeName) data.selectedIcon else data.unSelectedIcon,
                                    contentDescription = data.text,
                                    colorFilter = if (selectedRouteState.value == data.routeName) {
                                        ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                                    } else null
                                )
                            },
                        )
                    }
                }
            }
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                DestinationsNavHost(
                    navGraph = NavGraphs.home,
                    engine = getAnimatedNavHostEngine(),
                    navController = navController
                )
            }
        }
    }


    private fun getBottomNavBarItemList(): ArrayList<BottomAppBarItemData> {
        val bottomNavList = arrayListOf<BottomAppBarItemData>()
        bottomNavList.add(
            BottomAppBarItemData(
                getString(R.string.home),
                Icons.Filled.Home,
                Icons.Outlined.Home,
                HomeScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                getString(R.string.search),
                Icons.Filled.Search,
                Icons.Outlined.Search,
                AddPostScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                getString(R.string.add_post),
                Icons.Filled.AddCircle,
                Icons.Outlined.AddCircleOutline,
                AddPostScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                getString(R.string.chat),
                Icons.Filled.ChatBubble,
                Icons.Outlined.ChatBubbleOutline,
                AddPostScreenDestination.route
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                getString(R.string.profile),
                Icons.Filled.Person,
                Icons.Outlined.Person,
                UserProfileScreenDestination.route
            )
        )
        return bottomNavList
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogHelper() {

}

@Preview
@Composable
fun PreviewAlertDialogHelper() {
    AlertDialogHelper()
}