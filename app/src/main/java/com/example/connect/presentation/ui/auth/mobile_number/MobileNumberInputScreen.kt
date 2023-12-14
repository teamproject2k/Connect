package com.example.connect.presentation.ui.auth.mobile_number

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.presentation.ui.common.AppOutlinedTextField
import com.example.connect.presentation.ui.common.LoaderButton
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight48
import com.example.connect.presentation.ui.common.TopPageSection
import com.example.connect.presentation.ui.destinations.OTPScreenDestination
import com.example.connect.presentation.ui.destinations.UserDetailsScreenDestination
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.ui.home.base_screen.HomeActivity
import com.example.connect.presentation.utils.AuthenticationNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.validation.Validator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalComposeUiApi::class)
@AuthenticationNavGraph(start = true)
@Destination
@Composable
fun MobileNumberInputScreen(navigator: DestinationsNavigator) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: MobileNumberInputViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val context = LocalContext.current
    HandleSendOTPState(viewModel, navigator, context)
    HandleGetUserDetailsState(viewModel, navigator, context)
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.log_in),
                buildAnnotatedString {
                    append(
                        stringResource(R.string.an_otp_will_be_send_to_the_below_entered_mobile_number)
                    )
                }
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                MobileInputTextField(viewModel)
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    loadingText = stringResource(R.string.sending_otp),
                    buttonText = stringResource(id = R.string.get_otp),
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(viewModel, context)
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

@Composable
private fun HandleGetUserDetailsState(
    viewModel: MobileNumberInputViewModel,
    navigator: DestinationsNavigator,
    context: Context
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val userDetailsState = viewModel.getUserDetailsStateFlow.collectAsState().value
    when (userDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isExceptionHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (userDetailsState.data == null) {
                navigator.navigate(UserDetailsScreenDestination())
                navigator.popBackStack()
            } else {
                viewModel.sharedPreference.isUserDetailsEntered = true
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                LocalActivity.current.finish()
            }
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Success
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    if (userDetailsState.message.isNullOrBlank() || userDetailsState.message == ErrorCodes.NoUserFound) context.getString(
                        R.string.something_went_wrong
                    )
                    else userDetailsState.message.toString()
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Error
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "MobileNumberInputScreen",
                    userDetailsState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}

@Composable
private fun HandleSendOTPState(
    viewModel: MobileNumberInputViewModel,
    navigator: DestinationsNavigator,
    context: Context
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val sendOtpState = viewModel.sendOtpUIStateFlow.collectAsState().value
    when (sendOtpState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isExceptionHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Success
            if (sendOtpState.data?.first == FirebaseConstants.AutoLogin) {
                if (context.isNetworkAvailable()) {
                    viewModel.getUserDetails(sendOtpState.data.second)
                } else {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.no_internet_connection)
                }
            } else {
                navigator.navigate(
                    OTPScreenDestination(
                        viewModel.userMobileNumberState.value,
                        sendOtpState.data?.second.toString(),
                        viewModel.selectedCountryCodeState.value
                    )
                )
                viewModel.resetStateFlow()
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Error
                viewModel.snackBarMessageState.value =
                    if (sendOtpState.message.isNullOrBlank() || sendOtpState.message == ErrorCodes.NoUserFound) context.getString(
                        R.string.something_went_wrong
                    )
                    else sendOtpState.message.toString()
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ErrorTag,
                    "MobileNumberInputScreen",
                    sendOtpState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MobileInputTextField(viewModel: MobileNumberInputViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = viewModel.userMobileNumberState.value,
        textStyle = TextStyle(letterSpacing = 2.sp, fontWeight = FontWeight.Medium),
        onValueChange = { updatedValue ->
            if (updatedValue.length <= 10) {
                viewModel.userMobileNumberState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.mobile_number_can_t_be_greater_than_10_digits)
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
            }
        },
        label = {
            Text(text = stringResource(R.string.mobile_number))
        },
        leadingIcon = {
            Text(
                text = viewModel.selectedCountryCodeState.value,
                fontWeight = FontWeight.Medium
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

private fun handleButtonClick(
    viewModel: MobileNumberInputViewModel,
    context: Context
) {
    val mobileNumberValidationResponseCode =
        Validator.isValidMobileNumber(viewModel.userMobileNumberState.value)
    if (mobileNumberValidationResponseCode == 1) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_enter_mobile_number)
        FunctionHelper.vibrateDevice(context)
    } else if (mobileNumberValidationResponseCode == 2) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.invalid_mobile_number)
        FunctionHelper.vibrateDevice(context)
    } else if (mobileNumberValidationResponseCode == 0) {
        if (context.isNetworkAvailable()) {
            viewModel.sendOTP()
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
        }
    }
}
