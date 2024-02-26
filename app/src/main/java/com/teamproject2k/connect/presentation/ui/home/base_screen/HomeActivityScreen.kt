package com.teamproject2k.connect.presentation.ui.home.base_screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.SideEffect
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.utils.route
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.base.BaseActivity
import com.teamproject2k.connect.presentation.services.fcm.NotificationTypesEnum
import com.teamproject2k.connect.presentation.ui.NavGraphs
import com.teamproject2k.connect.presentation.ui.common.LoaderFullScreen
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.LogoutAlertDialog
import com.teamproject2k.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.teamproject2k.connect.presentation.ui.common.getHeightToMaintainAspectRatio
import com.teamproject2k.connect.presentation.ui.destinations.AddPostScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.FriendsAndPendingScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.HomeScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.SearchScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.models.BottomAppBarItemData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.showToast

@Composable
fun HomeActivityScreen(screenToNavigate: String) {
    val viewModel = hiltViewModel<HomeSharedViewModel>()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.getDeviceIdFromRemote(context)
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }
    CheckAndRequestNotificationPermission {
        SideEffect {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    HandleGetDeviceIdFlow(viewModel, context)
    HandleUserDetailsFlow(viewModel, context, screenToNavigate)
}

@Composable
private fun CheckAndRequestNotificationPermission(onPermissionRequest: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= 33) {

        val context = LocalContext.current
        val permissionCheckResult =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            // do not do anything if not granted
        } else {
            // Request a permission
            onPermissionRequest()
        }
    }
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
        RequestStatusEnum.Loading -> {
            LoaderFullScreen()
            isExceptionHandled = false
        }

        RequestStatusEnum.Exception -> {
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
                    ScreenNameEnum.HomeActivityScreen.name,
                    getDeviceIdState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.Success -> {
            // no need to handle
        }

        RequestStatusEnum.None -> {
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
private fun HandleUserDetailsFlow(
    viewModel: HomeSharedViewModel,
    context: Context,
    screenToNavigate: String
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }

    val getUserDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
    when (getUserDetailsState.status) {
        RequestStatusEnum.Loading -> {
            LoaderFullScreen(stringResource(R.string.getting_user_details))
            isExceptionHandled = false
        }

        RequestStatusEnum.Success -> {
            CreateUi(context, viewModel, screenToNavigate)
        }

        RequestStatusEnum.Exception -> {
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
                    ScreenNameEnum.HomeActivityScreen.name,
                    getUserDetailsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.None -> {

        }
    }
}


@Composable
private fun CreateUi(context: Context, viewModel: HomeSharedViewModel, screenToNavigate: String) {
    val selectedRouteState = rememberSaveable {
        mutableStateOf(HomeScreenDestination.route)
    }
    val bottomBarItems = remember {
        getHomeBottomNavBarItemList(context)
    }
    val navController = rememberNavController()

    var doubleBackToExitPressedOnce by remember {
        mutableStateOf(false)
    }
    val currentActivity = LocalActivity.current
    BackHandler {
        if (doubleBackToExitPressedOnce) {
            currentActivity.finish()
            return@BackHandler
        }
        doubleBackToExitPressedOnce = true
        context.showToast(context.getString(R.string.please_click_back_again_to_exit))
        Handler(Looper.getMainLooper()).postDelayed(
            { doubleBackToExitPressedOnce = false },
            ConstantsHelper.BACK_EXIT_RESET_TIME
        )
    }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            selectedRouteState.value = navEntry.route().route
            viewModel.isBottomBarHiddenState.value =
                selectedRouteState.value !in bottomBarItems.map { it.routeName }
        }
    }

    Scaffold(bottomBar = {
        Surface(tonalElevation = 4.dp, modifier = Modifier.animateContentSize()) {
            if (!viewModel.isBottomBarHiddenState.value) {
                NavigationBar(
                    modifier = Modifier
                        .height(
                            getHeightToMaintainAspectRatio(
                                noOfRows = 1, itemsRequiredPerRow = 6
                            )
                        )
                ) {
                    bottomBarItems.forEach { data ->
                        NavigationBarItem(
                            selected = selectedRouteState.value == data.routeName,
                            onClick = {
                                val isScreenPresentOnBackStack = try {
                                    navController.getBackStackEntry(data.routeName)
                                    true
                                } catch (exception: Exception) {
                                    false
                                }
                                if (isScreenPresentOnBackStack) {
                                    navController.popBackStack(data.routeName, false)
                                } else {
                                    navController.navigate(data.routeName) {
                                        launchSingleTop = true
                                    }
                                }
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
                            }
                        )
                    }
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
                navController = navController,
                startRoute = when (screenToNavigate) {
                    NotificationTypesEnum.FriendRequestReceived.name -> {
                        FriendsAndPendingScreenDestination
                    }

                    NotificationTypesEnum.FriendRequestAccepted.name -> {
                        FriendsAndPendingScreenDestination
                    }

                    else -> {
                        HomeScreenDestination
                    }
                }
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
private fun getHomeBottomNavBarItemList(context: Context): ArrayList<BottomAppBarItemData> {
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
            FriendsAndPendingScreenDestination.route
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

