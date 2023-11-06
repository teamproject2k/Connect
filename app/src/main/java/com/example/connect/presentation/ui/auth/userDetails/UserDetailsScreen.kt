package com.example.connect.presentation.ui.auth.userDetails

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
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
import com.example.connect.R
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppOutlinedTextField
import com.example.connect.presentation.ui.common.LoaderButton
import com.example.connect.presentation.ui.common.OutlinedTextFieldDisabledFeelsLikeEnabled
import com.example.connect.presentation.ui.common.SpacerHeight18
import com.example.connect.presentation.ui.common.SpacerHeight48
import com.example.connect.presentation.ui.common.TopPageSection
import com.example.connect.presentation.ui.home.HomeActivity
import com.example.connect.presentation.utils.AuthenticationNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.LocalActivity
import com.example.connect.presentation.utils.enums.ButtonLoadingState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
    val snackBarHostState = SnackbarHostState()
    HandleAddUserState(viewModel = viewModel, context = context)
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.create_an_account)
            )
            Column(modifier = Modifier.padding(16.dp)) {
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
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NameInputTextField(viewModel: UserDetailsViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = viewModel.userNameState.value,
        onValueChange = { updatedValue ->
            val pattern = Regex("[0-9!$@#%^&*()_+{}\\[\\]:;<>,.?~|]")
            if (!updatedValue.contains(pattern)) {
                viewModel.userNameState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.name_can_t_contain_digits_or_special_characters)
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
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
fun GenderPickerSection(viewModel: UserDetailsViewModel) {
    val genderList = stringArrayResource(id = R.array.gender_list)
    var isDialogVisible by remember {
        mutableStateOf(false)
    }
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = viewModel.selectedGenderState.value,
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
                modifier = Modifier.scale(if (isDialogVisible) -1f else 1f)
            )
        }
    ) {
        isDialogVisible = true
    }
    if (isDialogVisible) {
        DropdownMenu(
            expanded = true, onDismissRequest = { isDialogVisible = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(.9f),
        ) {
            genderList.forEach { item ->
                DropdownMenuItem(text = { Text(text = item) }, onClick = {
                    viewModel.selectedGenderState.value = item
                    isDialogVisible = false
                })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DOBPickerSection(viewModel: UserDetailsViewModel) {
    var showDatePickerState by remember {
        mutableStateOf(false)
    }
    val dateSelectionState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = viewModel.selectedDOBState.value,
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
                            viewModel.selectedDOBState.value =
                                FunctionHelper.getFormattedDate(dateSelectionState.selectedDateMillis!!)
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
    val uiState = viewModel.addUserStateFlow.collectAsState().value
    when (uiState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonLoadingState.Loading
        }

        RequestStatusEnum.SUCCESS -> {
            viewModel.sharedPreference.isUserDetailsEntered = true
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
            LocalActivity.current.finish()
            viewModel.currentButtonLoadingState.value = ButtonLoadingState.NotLoading
        }

        RequestStatusEnum.EXCEPTION -> {
            viewModel.snackBarMessageState.value =
                if (uiState.message.isNullOrBlank()) context.getString(R.string.something_went_wrong)
                else uiState.message.toString()
            viewModel.currentButtonLoadingState.value = ButtonLoadingState.NotLoading
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "UserDetailsScreen",
                uiState.message.toString()
            )
        }

        else -> {}
    }
}

private fun handleButtonClick(
    viewModel: UserDetailsViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    if (!viewModel.isValidName()) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_enter_valid_name)
        FunctionHelper.vibrateDevice(context)
    } else if (!viewModel.isGenderSelected()) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_select_a_gender)
        FunctionHelper.vibrateDevice(context)
    } else if (!viewModel.isDobSelected()) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_select_your_dob)
        FunctionHelper.vibrateDevice(context)
    } else {
        if (viewModel.fireBaseAuth.currentUser != null) {
            viewModel.createUserProfile()
        } else {
            context.showToast(context.getString(R.string.some_error_occurred_please_login_again))
            navigator.popBackStack()
        }
    }
}