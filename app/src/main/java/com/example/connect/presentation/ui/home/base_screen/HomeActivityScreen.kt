package com.example.connect.presentation.ui.home.base_screen

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.connect.R
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.example.connect.domain.logger.LoggingHelper
import com.example.connect.domain.logger.LoggingLevelEnum
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.NavGraphs
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.LogoutAlertDialog
import com.example.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.example.connect.presentation.ui.common.getHeightToMaintainAspectRatio
import com.example.connect.presentation.ui.destinations.AddPostScreenDestination
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.HomeScreenDestination
import com.example.connect.presentation.ui.destinations.SearchScreenDestination
import com.example.connect.presentation.ui.destinations.UserRequestScreenDestination
import com.example.connect.presentation.ui.models.BottomAppBarItemData
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.utils.route

@Composable
fun HomeActivityScreen() {
    val viewModel = hiltViewModel<HomeSharedViewModel>()
    val context = LocalContext.current
    viewModel.getDeviceIdFromRemote(context)
    HandleGetDeviceIdFlow(viewModel, context)
    HandleUserDetailsFlow(viewModel, context)
}

@Composable
private fun HandleGetDeviceIdFlow(viewModel: HomeSharedViewModel, context: Context) {
    val activity: BaseActivity = LocalActivity.current as BaseActivity
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    var showNewDeviceLoginAlertDialog by remember {
        mutableStateOf(false)
    }
    val getDeviceIdState = viewModel.deviceIdStateFlow.collectAsState().value
    when (getDeviceIdState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                when (getDeviceIdState.message) {
                    FirebaseErrorCodes.NO_USER_FOUND -> {
                        context.showToast(stringResource(id = R.string.some_error_occurred_please_login_again))
                        activity.logout()
                    }

                    FirebaseErrorCodes.NEW_LOGIN -> {
                        showNewDeviceLoginAlertDialog = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            showNewDeviceLoginAlertDialog = false
                            activity.logout()
                        }, ConstantsHelper.NEW_DEVICE_DIALOG_DISMISS_TIME)
                    }

                    else -> {
                        context.showToast(
                            getDeviceIdState.message
                                ?: stringResource(id = R.string.something_went_wrong)
                        )
                    }
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "HomeActivityScreen",
                    getDeviceIdState.message.toString()
                )
                isExceptionHandled = true
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
        LogoutAlertDialog {
            showNewDeviceLoginAlertDialog = false
        }
    }
}

@Composable
private fun HandleUserDetailsFlow(viewModel: HomeSharedViewModel, context: Context) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }

    val getUserDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
    when (getUserDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen(stringResource(R.string.getting_user_details))
            isExceptionHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            CreateUi(context)
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                if (getUserDetailsState.message == FirebaseErrorCodes.NO_USER_FOUND) {
                    (LocalActivity.current as BaseActivity).logout()
                } else {
                    if (getUserDetailsState.message.isNullOrBlank()) {
                        context.showToast(stringResource(id = R.string.something_went_wrong))
                    } else {
                        context.showToast(getUserDetailsState.message)
                    }
                }
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "HomeActivityScreen",
                    getUserDetailsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}


@Composable
private fun CreateUi(context: Context) {
    val selectedRouteState = rememberSaveable {
        mutableStateOf(HomeScreenDestination.route)
    }
    val navController = rememberNavController()


    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            selectedRouteState.value = it.route().route
        }
    }

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
                getHomeBottomNavBarItemList(context).forEach { data ->
                    NavigationBarItem(
                        selected = selectedRouteState.value == data.routeName,
                        onClick = {
                            navController.navigate(data.routeName) {
                                launchSingleTop = true
                            }
                            selectedRouteState.value = data.routeName
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


/**
 * Gets the list of items to be displayed in the bottom navigation bar of home activity.
 *
 * @param context The context of the application.
 * @return The list of items to be displayed in the bottom navigation bar.
 */
fun getHomeBottomNavBarItemList(context: Context): ArrayList<BottomAppBarItemData> {
    val bottomNavList = arrayListOf<BottomAppBarItemData>()
    bottomNavList.add(
        BottomAppBarItemData(
            context.getString(R.string.home),
            Icons.Filled.Home,
            Icons.Outlined.Home,
            HomeScreenDestination.route
        )
    )
    bottomNavList.add(
        BottomAppBarItemData(
            context.getString(R.string.search),
            Icons.Filled.Search,
            Icons.Outlined.Search,
            SearchScreenDestination.route
        )
    )
    bottomNavList.add(
        BottomAppBarItemData(
            context.getString(R.string.add_post),
            Icons.Filled.AddCircle,
            Icons.Outlined.AddCircleOutline,
            AddPostScreenDestination.route
        )
    )
    bottomNavList.add(
        BottomAppBarItemData(
            context.getString(R.string.requests),
            Icons.Filled.Handshake,
            Icons.Outlined.Handshake,
            UserRequestScreenDestination.route
        )
    )
    bottomNavList.add(
        BottomAppBarItemData(
            context.getString(R.string.profile),
            Icons.Filled.Person,
            Icons.Outlined.Person,
            CurrentUserProfileScreenDestination.route
        )
    )
    return bottomNavList
}

