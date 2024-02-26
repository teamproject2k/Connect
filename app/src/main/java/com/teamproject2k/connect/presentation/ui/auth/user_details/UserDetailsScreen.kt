package com.teamproject2k.connect.presentation.ui.auth.user_details

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.ui.common.AppOutlinedTextField
import com.teamproject2k.connect.presentation.ui.common.LoaderButton
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.OutlinedTextFieldDisabledFeelsLikeEnabled
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight18
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight48
import com.teamproject2k.connect.presentation.ui.common.TopPageSection
import com.teamproject2k.connect.presentation.ui.destinations.MobileNumberInputScreenDestination
import com.teamproject2k.connect.presentation.ui.enums.ButtonStateEnum
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.home.base_screen.HomeActivity
import com.teamproject2k.connect.presentation.utils.AuthenticationNavGraph
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.teamproject2k.connect.presentation.utils.FunctionHelper.showToast
import com.teamproject2k.connect.presentation.validation.Validator
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalComposeUiApi::class)
@AuthenticationNavGraph
@Destination
@Composable
fun UserDetailsScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: UserDetailsViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.create_an_account),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                NameInputTextField(viewModel)
                SpacerHeight18()
                Row(modifier = Modifier.fillMaxWidth()) {
                    GenderPickerSection(viewModel)
                }
                SpacerHeight18()
                DOBPickerSection(viewModel = viewModel)
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    loadingText = stringResource(R.string.creating_account),
                    buttonText = stringResource(id = R.string.create_account),
                    isEnabled = isButtonEnabled(viewModel),
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(viewModel, context, navigator)
                    }
                )
            }
        }
    }
    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            snackBarHostState.showSnackbar(
                viewModel.snackBarMessageState.value,
                duration = SnackbarDuration.Short
            )
            viewModel.snackBarMessageState.value = ""
        }
    }
    HandleAddUserState(viewModel = viewModel, context = context)
}

@Composable
private fun NameInputTextField(viewModel: UserDetailsViewModel) {
    val context = LocalContext.current
    AppOutlinedTextField(
        value = viewModel.userNameState.value,
        onValueChange = { updatedValue ->
            if (updatedValue.length <= ConstantsHelper.NAME_MAX_CHAR_LIMIT) {
                viewModel.userNameState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value = context.getString(
                    R.string.name_cannot_be_greater_than_max_characters,
                    ConstantsHelper.NAME_MAX_CHAR_LIMIT
                )
                FunctionHelper.vibrateDevice(context)
            }
        },
        label = {
            Text(text = stringResource(R.string.full_name))
        },
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.Face,
                contentDescription = stringResource(id = R.string.full_name)
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderPickerSection(viewModel: UserDetailsViewModel) {
    val genderList = stringArrayResource(id = R.array.gender_list)
    var isDropdownMenuVisible by remember {
        mutableStateOf(false)
    }
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = viewModel.selectedGenderState.value,
        showEnabledByDefault = false,
        modifier = Modifier
            .fillMaxWidth(),
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.Person,
                contentDescription = stringResource(id = R.string.gender)
            )
        },
        label = { Text(text = stringResource(R.string.gender)) },
        trailingIcon = {
            Image(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(id = R.string.gender),
                modifier = Modifier.scale(if (isDropdownMenuVisible) -1f else 1f)
            )
        }
    ) {
        isDropdownMenuVisible = true
    }
    if (isDropdownMenuVisible) {
        DropdownMenu(
            expanded = true, onDismissRequest = { isDropdownMenuVisible = false },
            modifier = Modifier
                .fillMaxWidth(.9f),
        ) {
            genderList.forEach { item ->
                DropdownMenuItem(text = { Text(text = item) }, onClick = {
                    viewModel.selectedGenderState.value = item
                    isDropdownMenuVisible = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DOBPickerSection(viewModel: UserDetailsViewModel) {
    var showDatePickerState by remember {
        mutableStateOf(false)
    }
    val dateSelectionState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        showEnabledByDefault = false,
        value = if (viewModel.selectedDOBState.longValue != -1L) FunctionHelper.getFormattedDateTime(
            viewModel.selectedDOBState.longValue
        ) else "",
        modifier = Modifier
            .fillMaxWidth(),
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = stringResource(id = R.string.date_of_birth)
            )
        },
        label = { Text(text = stringResource(R.string.date_of_birth)) },
    ) {
        showDatePickerState = true
    }
    if (showDatePickerState) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerState = false },
            confirmButton = {
                Text(text = stringResource(R.string.ok), modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clickable {
                        if (dateSelectionState.selectedDateMillis != null) {
                            viewModel.selectedDOBState.longValue =
                                dateSelectionState.selectedDateMillis!!
                        }
                        showDatePickerState = false
                    })
            },
            dismissButton = {
                Text(text = stringResource(R.string.cancel), modifier = Modifier
                    .padding(bottom = 16.dp, end = 16.dp)
                    .clickable {
                        showDatePickerState = false
                    })
            }
        ) {
            DatePicker(state = dateSelectionState, showModeToggle = true, dateValidator = {
                val calender = Calendar.getInstance()
                val enteredDate = Date(it)
                enteredDate.before(calender.time)
            })
        }
    }
}

@Composable
private fun HandleAddUserState(
    viewModel: UserDetailsViewModel,
    context: Context
) {
    var isResponseHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val uiState = viewModel.addUserStateFlow.collectAsState().value
    when (uiState.status) {
        RequestStatusEnum.Loading -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                viewModel.sharedPreference.isUserDetailsEntered = true
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.NotLoading
                LocalActivity.current.finish()
                isResponseHandled = true
            }
        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    if (uiState.message.isNullOrBlank() || uiState.message == FirebaseErrorCodes.FCM_TOKEN_NOT_GENERATED) context.getString(
                        R.string.something_went_wrong
                    )
                    else uiState.message.toString()
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.NotLoading
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.UserDetailsScreen.name,
                    uiState.message.toString()
                )
                isResponseHandled = true
            }
        }

        else -> {}
    }
}

/**
 * Handles the button click event in the [UserDetailsScreen].
 *
 * @param viewModel The [UserDetailsViewModel] instance.
 * @param context The current context.
 * @param navigator The [DestinationsNavigator] instance.
 */
private fun handleButtonClick(
    viewModel: UserDetailsViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    when (val userNameValidationResponseCode =
        Validator.isValidName(viewModel.userNameState.value)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_enter_name)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_name)
            FunctionHelper.vibrateDevice(context)
            return
        }

        3 -> {
            viewModel.snackBarMessageState.value =
                context.getString(
                    R.string.name_cannot_be_greater_than_max_characters,
                    ConstantsHelper.NAME_MAX_CHAR_LIMIT
                )
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (userNameValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    when (val genderValidationResponseCode =
        Validator.isValidGender(viewModel.selectedGenderState.value, context)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_select_your_gender)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_gender)
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (genderValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    when (val dobValidationResponseCode =
        Validator.isValidDob(viewModel.selectedDOBState.longValue)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_select_your_date_of_birth)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_date_of_birth)
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (dobValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    if (viewModel.fireBaseAuth.currentUser != null) {
        if (context.isNetworkAvailable()) {
            viewModel.createUserProfile()
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
            FunctionHelper.vibrateDevice(context)
        }
    } else {
        context.showToast(context.getString(R.string.some_error_occurred_please_login_again))
        navigator.popBackStack()
        navigator.navigate(MobileNumberInputScreenDestination)
    }
}

/**
 * Checks if the button should be enabled.
 * @param viewModel The view model.
 * @return True if the button should be enabled, false otherwise.
 */
private fun isButtonEnabled(viewModel: UserDetailsViewModel): Boolean {
    var result = true
    if (viewModel.userNameState.value.isBlank() || viewModel.selectedGenderState.value.isBlank() || viewModel.selectedDOBState.longValue == -1L) {
        result = false
    }
    return result
}