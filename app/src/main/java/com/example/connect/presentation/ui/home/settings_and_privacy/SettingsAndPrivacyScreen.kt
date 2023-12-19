package com.example.connect.presentation.ui.home.settings_and_privacy

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum.EXCEPTION
import com.example.connect.common.RequestStatusEnum.LOADING
import com.example.connect.common.RequestStatusEnum.NONE
import com.example.connect.common.RequestStatusEnum.SUCCESS
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.common.AppTopAppBar
import com.example.connect.presentation.ui.common.DividerLightGrayAlpha50
import com.example.connect.presentation.ui.common.LoaderDialog
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.VisibilityItem
import com.example.connect.presentation.ui.common.VisibilityScopeBottomSheetItem
import com.example.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph
@Destination
@Composable
fun SettingsScreen() {

    val homeSharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: SettingsAndPrivacyViewModel = hiltViewModel()
    val context = LocalContext.current

    if (viewModel.isFirstTimeSetup) {
        viewModel.setUpData(homeSharedViewModel.usersDetails, context)
    }

    val coroutineScope = rememberCoroutineScope()
    val genderVisibility = viewModel.genderVisibilityState.value
    val dobVisibility = viewModel.dobVisibilityState.value
    val friendListVisibility = viewModel.friendListVisibilityState.value

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
        AppTopAppBar(title = stringResource(R.string.settings_and_privacy))
    }) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SettingsAndPrivacyDropdownItem(
                itemNameId = R.string.gender_privacy,
                drawableId = genderVisibility.drawableId,
                scopeName = genderVisibility.scopeName
            ) {
                showGenderBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyDropdownItem(
                itemNameId = R.string.date_of_birth_privacy,
                drawableId = dobVisibility.drawableId,
                scopeName = dobVisibility.scopeName
            ) {
                showDobBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyDropdownItem(
                itemNameId = R.string.friend_list_privacy,
                drawableId = friendListVisibility.drawableId,
                scopeName = friendListVisibility.scopeName
            ) {
                showFriendListBottomSheet = true
            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyClickableItem(itemNameId = R.string.blocked_users) {

            }
            DividerLightGrayAlpha50()
            SettingsAndPrivacyClickableItem(itemNameId = R.string.requested_users) {

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
                        homeSharedViewModel.usersDetails
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
    HandleUpdateGenderVisibilityStateFlow(viewModel, homeSharedViewModel, context)
    HandleUpdateDobVisibilityStateFlow(viewModel, homeSharedViewModel, context)
    HandleUpdateFriendListVisibilityStateFlow(viewModel, homeSharedViewModel, context)
}

@Composable
private fun SettingsAndPrivacyDropdownItem(
    itemNameId: Int,
    drawableId: Int,
    scopeName: String,
    onItemClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(itemNameId),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 24.dp)
                .weight(1f),
            fontSize = 14.sp
        )
        VisibilityItem(
            modifier = Modifier.padding(end = 16.dp),
            drawableId = drawableId,
            scopeName = scopeName
        ) {
            onItemClick()
        }
    }
}

@Composable
private fun SettingsAndPrivacyClickableItem(itemNameId: Int, onItemClick: () -> (Unit)) {
    Text(
        text = stringResource(itemNameId),
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 24.dp)
            .clickable { onItemClick() },
        fontSize = 14.sp
    )
}

@Composable
private fun GenderVisibilityScopeBottomSheet(
    modifier: Modifier,
    viewModel: SettingsAndPrivacyViewModel,
    userDetails: UsersBean,
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.genderVisibilityScopeList.forEach { genderScope ->
            VisibilityScopeBottomSheetItem(genderScope) {
                viewModel.genderVisibilityState.value = genderScope
                if (viewModel.genderVisibilityState.value.scopeEnum.name != userDetails.genderVisibility) {
                    viewModel.updateGenderVisibility(userDetails.firebaseUserId)
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
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.dobVisibilityScopeList.forEach { dobScope ->
            VisibilityScopeBottomSheetItem(dobScope) {
                viewModel.dobVisibilityState.value = dobScope
                if (viewModel.dobVisibilityState.value.scopeEnum.name != userDetails.dobVisibility) {
                    viewModel.updateDobVisibility(userDetails.firebaseUserId)
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
    onDismissRequest: () -> Unit
) {
    Column(modifier = modifier) {
        viewModel.friendListVisibilityScopeList.forEach { friendListScope ->
            VisibilityScopeBottomSheetItem(friendListScope) {
                viewModel.friendListVisibilityState.value = friendListScope
                if (viewModel.friendListVisibilityState.value.scopeEnum.name != userDetails.friendListVisibility) {
                    viewModel.updateFriendListVisibility(userDetails.firebaseUserId)
                }
                onDismissRequest()
            }
        }
    }
}

@Composable
fun HandleUpdateGenderVisibilityStateFlow(
    viewModel: SettingsAndPrivacyViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val updateUserGenderState = viewModel.updateGenderVisibilityStateFlow.collectAsState().value
    when (updateUserGenderState.status) {
        LOADING -> {
            LoaderDialog(stringResource(R.string.updating_gender_visibility))
            isResponseHandled = false
        }

        SUCCESS -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                homeSharedViewModel.usersDetails.genderVisibility =
                    viewModel.genderVisibilityState.value.scopeEnum.name
                isResponseHandled = true
            }
        }

        EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateUserGenderState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "SettingsAndPrivacyScreen",
                    updateUserGenderState.message.toString()
                )
                isResponseHandled = true
            }
        }

        NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleUpdateDobVisibilityStateFlow(
    viewModel: SettingsAndPrivacyViewModel,
    homeSharedViewModel: HomeSharedViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val updateDobVisibilityState = viewModel.updateDobVisibilityStateFlow.collectAsState().value
    when (updateDobVisibilityState.status) {
        LOADING -> {
            LoaderDialog(stringResource(R.string.updating_dob_visibility))
            isResponseHandled = false
        }

        SUCCESS -> {
            if (!isResponseHandled) {
                homeSharedViewModel.usersDetails.dobVisibility =
                    viewModel.dobVisibilityState.value.scopeEnum.name
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                isResponseHandled = true
            }
        }

        EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateDobVisibilityState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "SettingsAndPrivacyScreen",
                    updateDobVisibilityState.message.toString()
                )
                isResponseHandled = true
            }
        }

        NONE -> {
            // no need to handle this
        }
    }
}

@Composable
fun HandleUpdateFriendListVisibilityStateFlow(
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
        LOADING -> {
            LoaderDialog(stringResource(R.string.updating_friend_list_visibility))
            isResponseHandled = false
        }

        SUCCESS -> {
            if (!isResponseHandled) {
                homeSharedViewModel.usersDetails.friendListVisibility =
                    viewModel.friendListVisibilityState.value.scopeEnum.name
                context.showToast(stringResource(R.string.visibility_updated_successfully))
                isResponseHandled = true
            }
        }

        EXCEPTION -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    updateFriendListVisibilityState.message
                        ?: stringResource(id = R.string.some_error_occurred)
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "SettingsAndPrivacyScreen",
                    updateFriendListVisibilityState.message.toString()
                )
                isResponseHandled = true
            }
        }

        NONE -> {
            // no need to handle this
        }
    }
}