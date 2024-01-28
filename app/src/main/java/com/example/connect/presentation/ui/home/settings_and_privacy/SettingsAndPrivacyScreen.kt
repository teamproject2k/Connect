package com.example.connect.presentation.ui.home.settings_and_privacy

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.VisibilityItem
import com.example.connect.presentation.ui.common.VisibilityScopeBottomSheetItem
import com.example.connect.presentation.ui.destinations.BlockedListScreenDestination
import com.example.connect.presentation.ui.destinations.RequestedListScreenDestination
import com.example.connect.presentation.ui.destinations.SavedPostsScreenDestination
import com.example.connect.presentation.ui.enums.ScreenNameEnum
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SettingsAndPrivacyScreen(navigator: DestinationsNavigator) {

    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: SettingsAndPrivacyViewModel = hiltViewModel()
    val context = LocalContext.current

    if (viewModel.isFirstTimeSetup) {
        viewModel.setUpData(homeSharedViewModel.usersDetails, context)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = SnackbarHostState()

    var showGenderBottomSheet by remember {
        mutableStateOf(false)
    }
    var showDobBottomSheet by remember {
        mutableStateOf(false)
    }
    var showFriendListBottomSheet by remember {
        mutableStateOf(false)
    }

    Scaffold(topBar = {
        AppTopAppBar(
            title = stringResource(R.string.settings_and_privacy),
            showNavigationIcon = true,
            onNavigationIconClick = { navigator.popBackStack() }
        )
    }, snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SettingsAndPrivacySectionWithVisibilityItem(
                itemName = stringResource(id = R.string.gender_privacy),
                drawableId = viewModel.genderVisibilityState.value.drawableId,
                scopeName = viewModel.genderVisibilityState.value.scopeName
            ) {
                showGenderBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacySectionWithVisibilityItem(
                itemName = stringResource(id = R.string.date_of_birth_privacy),
                drawableId = viewModel.dobVisibilityState.value.drawableId,
                scopeName = viewModel.dobVisibilityState.value.scopeName
            ) {
                showDobBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacySectionWithVisibilityItem(
                itemName = stringResource(id = R.string.friend_list_privacy),
                drawableId = viewModel.friendListVisibilityState.value.drawableId,
                scopeName = viewModel.friendListVisibilityState.value.scopeName
            ) {
                showFriendListBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyClickableItem(itemName = stringResource(id = R.string.saved_posts)) {
                navigator.navigate(SavedPostsScreenDestination())
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyClickableItem(itemName = stringResource(id = R.string.blocked_users)) {
                navigator.navigate(BlockedListScreenDestination())
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyClickableItem(itemName = stringResource(id = R.string.requested_users)) {
                navigator.navigate(RequestedListScreenDestination())
            }
            DividerLightGrayAlpha50()
            if (showGenderBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showGenderBottomSheet = false },
                    shape = RoundedCornerShape(
                        topEnd = ConstantsHelper.BottomSheetRoundness,
                        topStart = ConstantsHelper.BottomSheetRoundness
                    )
                ) {
                    GenderVisibilityScopeBottomSheet(
                        modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                        viewModel = viewModel,
                        homeSharedViewModel.usersDetails,
                        context
                    ) {
                        showGenderBottomSheet = false
                    }
                }
            }
            if (showDobBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showDobBottomSheet = false },
                    shape = RoundedCornerShape(
                        topEnd = ConstantsHelper.BottomSheetRoundness,
                        topStart = ConstantsHelper.BottomSheetRoundness
                    )
                ) {
                    DobVisibilityScopeBottomSheet(
                        modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                        viewModel = viewModel,
                        homeSharedViewModel.usersDetails,
                        context
                    ) {
                        showDobBottomSheet = false
                    }
                }
            }
            if (showFriendListBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFriendListBottomSheet = false },
                    shape = RoundedCornerShape(
                        topEnd = ConstantsHelper.BottomSheetRoundness,
                        topStart = ConstantsHelper.BottomSheetRoundness
                    )
                ) {
                    FriendListVisibilityScopeBottomSheet(
                        modifier = Modifier.padding(bottom = ConstantsHelper.NavigationBarHeight),
                        viewModel = viewModel,
                        homeSharedViewModel.usersDetails,
                        context
                    ) {
                        showFriendListBottomSheet = false
                    }
                }
            }
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
    HandleUpdateGenderVisibilityState(viewModel, homeSharedViewModel, context)
    HandleUpdateDobVisibilityState(viewModel, homeSharedViewModel, context)
    HandleUpdateFriendListVisibilityState(viewModel, homeSharedViewModel, context)
}

@Composable
private fun SettingsAndPrivacySectionWithVisibilityItem(
    itemName: String,
    @DrawableRes drawableId: Int,
    scopeName: String,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = itemName,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f),
            fontSize = 14.sp
        )
        VisibilityItem(
            drawableId = drawableId,
            scopeName = scopeName
        ) {
            onItemClick()
        }
    }
}

@Composable
private fun SettingsAndPrivacyClickableItem(
    itemName: String,
    onItemClick: () -> (Unit)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick()
            }
            .padding(16.dp)
    ) {
        Text(
            text = itemName,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GenderVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: SettingsAndPrivacyViewModel,
    userDetails: UsersBean,
    context: Context,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.genderVisibilityScopeList.forEach { genderScope ->
            VisibilityScopeBottomSheetItem(genderScope) {
                if (context.isNetworkAvailable()) {
                    if (viewModel.genderVisibilityState.value.scopeEnum.name != genderScope.scopeEnum.name) {
                        viewModel.updateGenderVisibility(userDetails.firebaseUserId, genderScope)
                    }
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
                onDismissRequest()
            }
        }
    }
}

@Composable
private fun DobVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: SettingsAndPrivacyViewModel,
    userDetails: UsersBean,
    context: Context,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.dobVisibilityScopeList.forEach { dobScope ->
            VisibilityScopeBottomSheetItem(dobScope) {
                if (context.isNetworkAvailable()) {
                    if (viewModel.dobVisibilityState.value.scopeEnum.name != dobScope.scopeEnum.name) {
                        viewModel.updateDobVisibility(userDetails.firebaseUserId, dobScope)
                    }
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
                onDismissRequest()
            }
        }
    }
}

@Composable
private fun FriendListVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: SettingsAndPrivacyViewModel,
    userDetails: UsersBean,
    context: Context,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.friendListVisibilityScopeList.forEach { friendListScope ->
            VisibilityScopeBottomSheetItem(friendListScope) {
                if (context.isNetworkAvailable()) {
                    if (viewModel.friendListVisibilityState.value.scopeEnum.name != friendListScope.scopeEnum.name) {
                        viewModel.updateFriendListVisibility(
                            userDetails.firebaseUserId,
                            friendListScope
                        )
                    }
                } else {
                    viewModel.snackBarMessageState.value =
                        context.getString(R.string.no_internet_connection)
                    FunctionHelper.vibrateDevice(context)
                }
                onDismissRequest()
            }
        }
    }
}

@Composable
fun HandleUpdateGenderVisibilityState(
    viewModel: SettingsAndPrivacyViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val updateUserGenderState = viewModel.updateGenderVisibilityStateFlow.collectAsState().value
    when (updateUserGenderState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.updating_gender_visibility))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                val scopeName = updateUserGenderState.data ?: return
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                homeSharedViewModel.usersDetails.genderVisibility = scopeName.scopeEnum.name
                viewModel.genderVisibilityState.value = scopeName
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateUserGenderState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SettingsAndPrivacyScreen.name,
                    updateUserGenderState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleUpdateDobVisibilityState(
    viewModel: SettingsAndPrivacyViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val updateDobVisibilityState = viewModel.updateDobVisibilityStateFlow.collectAsState().value
    when (updateDobVisibilityState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.updating_dob_visibility))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                val scopeName = updateDobVisibilityState.data ?: return
                homeSharedViewModel.usersDetails.dobVisibility = scopeName.scopeEnum.name
                viewModel.dobVisibilityState.value = scopeName
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateDobVisibilityState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SettingsAndPrivacyScreen.name,
                    updateDobVisibilityState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleUpdateFriendListVisibilityState(
    viewModel: SettingsAndPrivacyViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val updateFriendListVisibilityState =
        viewModel.updateFriendListVisibilityStateFlow.collectAsState().value
    when (updateFriendListVisibilityState.status) {
        RequestStatusEnum.Loading -> {
            LoaderDialog(stringResource(R.string.updating_friend_list_visibility))
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                val scopeName = updateFriendListVisibilityState.data ?: return
                homeSharedViewModel.usersDetails.friendListVisibility = scopeName.scopeEnum.name
                viewModel.friendListVisibilityState.value = scopeName
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateFriendListVisibilityState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.SettingsAndPrivacyScreen.name,
                    updateFriendListVisibilityState.message.toString()
                )
                isResponseHandled = true
            }
        }

        RequestStatusEnum.None -> {
            // no need to handle this
        }
    }
}