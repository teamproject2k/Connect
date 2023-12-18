package com.example.connect.presentation.ui.auth.otp_input

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.FirebaseConstants
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.presentation.ui.common.ColorsHelper.warning
import com.example.connect.presentation.ui.common.LoaderButton
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.OutlinedTextFieldNoLabel
import com.example.connect.presentation.ui.common.SpacerHeight18
import com.example.connect.presentation.ui.common.SpacerHeight48
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TitleMessageIconOkCancelDialog
import com.example.connect.presentation.ui.common.TopPageSection
import com.example.connect.presentation.ui.destinations.MobileNumberInputScreenDestination
import com.example.connect.presentation.ui.destinations.UserDetailsScreenDestination
import com.example.connect.presentation.ui.enums.ButtonStateEnum
import com.example.connect.presentation.ui.home.base_screen.HomeActivity
import com.example.connect.presentation.utils.AuthenticationNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.validation.Validator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalComposeUiApi::class)
@AuthenticationNavGraph
@Destination
@Composable
fun OTPScreen(
    navigator: DestinationsNavigator,
    mobileNumber: String,
    verificationId: String,
    countryCode: String
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: OtpInputViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    viewModel.mobileNumber = mobileNumber
    viewModel.verificationId = verificationId
    viewModel.countryCode = countryCode
    HandleVerifyOTPState(viewModel, navigator, context)
    HandleUserDetailsState(viewModel, navigator, context)
    HandleResendOTPState(viewModel, context)
    HandleBackPressed(navigator)
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.enter_otp),
                buildAnnotatedString {
                    append(
                        stringResource(
                            id = R.string.an_otp_has_been_sent_to,
                            countryCode,
                            mobileNumber
                        )
                    )
                    append(" ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("$countryCode $mobileNumber.")
                    }
                }
            )
            Column(modifier = Modifier.padding(16.dp)) {
                OTPField(viewModel)
                SpacerHeight18()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(R.string.didn_t_receive_otp), fontSize = 12.sp)
                    SpacerWidth6()
                    OTPTimer(viewModel)
                }
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    buttonText = stringResource(id = R.string.verify_otp),
                    loadingText = stringResource(R.string.verifying_otp),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OTPTimer(viewModel: OtpInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val countDownTimeLeft =
        viewModel.timeLeftFlow.collectAsState(initial = ConstantsHelper.OTP_TIMEOUT_TIME)
    if (viewModel.showTimerState.value) {
        Text(
            text = stringResource(
                id = R.string.string_digit_string_placeholder,
                stringResource(R.string.resend_in),
                countDownTimeLeft.value,
                if (countDownTimeLeft.value > 1) {
                    stringResource(id = R.string.secs)
                } else {
                    stringResource(id = R.string.sec)
                }
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    } else {
        Text(
            text = stringResource(R.string.resend_now),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                keyboardController?.hide()
                viewModel.resendOtp()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPField(viewModel: OtpInputViewModel) {
    val focusRequesterList = List(ConstantsHelper.OTP_CHAR_COUNT) { FocusRequester() }
    var wasValueEntered by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center
    ) {
        repeat(ConstantsHelper.OTP_CHAR_COUNT) { index ->
            val enteredValue = viewModel.otpState.value[index].toString()
            OutlinedTextFieldNoLabel(
                value = enteredValue.ifBlank { "" },
                onValueChange = { updatedValue ->
                    val valueToFill =
                        if (updatedValue.isDigitsOnly() && updatedValue.isNotBlank()) updatedValue[0].toString() else " "
                    val updatedOTP = if (index == 0) {
                        "$valueToFill${viewModel.otpState.value.substring(1)}"
                    } else {
                        val currentOtp = viewModel.otpState.value
                        "${
                            currentOtp.substring(
                                0,
                                index
                            )
                        }$valueToFill${currentOtp.substring(index + 1)}"
                    }
                    viewModel.otpState.value = updatedOTP
                    if (valueToFill.isDigitsOnly() && index + 1 != ConstantsHelper.OTP_CHAR_COUNT) {
                        focusRequesterList[index + 1].requestFocus()
                    } else if (valueToFill.isBlank()) {
                        wasValueEntered = true
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .focusRequester(focusRequesterList[index])
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace) {
                            if (wasValueEntered) {
                                wasValueEntered = false
                            } else {
                                if (index != 0) {
                                    focusRequesterList[index - 1].requestFocus()
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
            )
            if (index + 1 != ConstantsHelper.OTP_CHAR_COUNT) {
                SpacerWidth8()
            }
        }
    }
    LaunchedEffect(key1 = true) {
        if (focusRequesterList.isNotEmpty()) {
            focusRequesterList[0].requestFocus()
        }
    }
}


@Composable
fun HandleUserDetailsState(
    viewModel: OtpInputViewModel,
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
                navigator.popBackStack(MobileNumberInputScreenDestination.route, inclusive = true)
                navigator.navigate(UserDetailsScreenDestination())
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
                    ConstantsHelper.ERROR_TAG,
                    "OTPInputScreen",
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
fun HandleVerifyOTPState(
    viewModel: OtpInputViewModel,
    navigator: DestinationsNavigator,
    context: Context
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val verifyOtpState = viewModel.verifyOtpStateFlow.collectAsState().value
    when (verifyOtpState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isExceptionHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (verifyOtpState.data != null) {
                if (context.isNetworkAvailable()) {
                    viewModel.getUserDetails(verifyOtpState.data.uid)
                } else {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.no_internet_connection)
                }
            } else {
                context.showToast(context.getString(R.string.some_error_occurred_please_login_again))
                navigator.popBackStack()
            }
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Success
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    if (verifyOtpState.message.isNullOrBlank() || verifyOtpState.message == ErrorCodes.NoUserFound) context.getString(
                        R.string.something_went_wrong
                    )
                    else verifyOtpState.message.toString()
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Error
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "OTPInputScreen",
                    verifyOtpState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }
}

@Composable
fun HandleResendOTPState(
    viewModel: OtpInputViewModel,
    context: Context
) {
    var isExceptionHandled by rememberSaveable {
        mutableStateOf(false)
    }
    val resendOtpState = viewModel.resendOtpStateFlow.collectAsState().value
    when (resendOtpState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isExceptionHandled = false
        }

        RequestStatusEnum.SUCCESS -> {
            if (resendOtpState.data?.first == FirebaseConstants.AutoLogin) {
                if (context.isNetworkAvailable()) {
                    viewModel.getUserDetails(resendOtpState.data.second)
                } else {
                    viewModel.snackBarMessageState.value =
                        stringResource(id = R.string.no_internet_connection)
                }
            } else {
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.otp_sent_successfully)
            }
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Success
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    if (resendOtpState.message.isNullOrBlank() || resendOtpState.message == ErrorCodes.NoUserFound) context.getString(
                        R.string.something_went_wrong
                    )
                    else resendOtpState.message.toString()
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Error
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    "OTPInputScreen",
                    resendOtpState.message.toString()
                )
                isExceptionHandled = true
            }
        }

        RequestStatusEnum.NONE -> {

        }
    }

}

private fun handleButtonClick(viewModel: OtpInputViewModel, context: Context) {
    val otpValidationResponseCode = Validator.isValidOTP(viewModel.otpState.value)
    if (otpValidationResponseCode == 1) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_enter_an_otp)
        FunctionHelper.vibrateDevice(context)
    } else if (otpValidationResponseCode == 2) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.invalid_otp)
        FunctionHelper.vibrateDevice(context)
    } else if (otpValidationResponseCode == 0) {
        if (context.isNetworkAvailable()) {
            viewModel.verifyOTP(viewModel.verificationId)
        } else {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.no_internet_connection)
        }
    }
}

@Composable
private fun HandleBackPressed(navigator: DestinationsNavigator) {

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    BackHandler {
        showLogoutDialog = true
    }

    if (showLogoutDialog) {
        TitleMessageIconOkCancelDialog(
            title = stringResource(id = R.string.go_back),
            iconTint = warning(),
            imageVector = Icons.Default.Warning,
            subTitle = stringResource(id = R.string.do_you_want_to_edit_your_phone_number),
            onCancel = { showLogoutDialog = false }) {
            showLogoutDialog = false
            navigator.popBackStack()
        }
    }
}
