package com.example.connect.presentation.ui.auth.otp_input

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import com.example.connect.presentation.ui.auth.destinations.MobileNumberInputScreenDestination
import com.example.connect.presentation.ui.auth.destinations.UserDetailsScreenDestination
import com.example.connect.presentation.ui.common.LoaderButton
import com.example.connect.presentation.ui.common.OutlinedTextFieldNoLabel
import com.example.connect.presentation.ui.common.SpacerHeight18
import com.example.connect.presentation.ui.common.SpacerHeight48
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TopPageSection
import com.example.connect.presentation.ui.home.HomeActivity
import com.example.connect.presentation.utils.AuthenticationNavGraph
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.LocalActivity
import com.example.connect.presentation.utils.enums.ButtonLoadingEnum
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

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.enter_otp)
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
                    OTPTTimer(viewModel)
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
fun OTPTTimer(viewModel: OtpInputViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(key1 = viewModel.showTimerState.value) {
        if (viewModel.showTimerState.value) {
            viewModel.startTimer()
        }
    }
    if (viewModel.showTimerState.value) {
        Text(
            text = stringResource(
                id = R.string.string_digit_string_placeholder,
                stringResource(R.string.resend_in),
                viewModel.timeLeftState.longValue,
                if (viewModel.timeLeftState.longValue > 1) {
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
    val focusRequesterList = List(ConstantsHelper.OTPCharCount) { FocusRequester() }
    var wasValueEntered by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center
    ) {
        repeat(ConstantsHelper.OTPCharCount) { index ->
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
                    if (valueToFill.isDigitsOnly() && index + 1 != ConstantsHelper.OTPCharCount) {
                        focusRequesterList[index + 1].requestFocus()
                    } else if (valueToFill.isBlank()) {
                        wasValueEntered = true
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(textAlign = TextAlign.Center, color = Color.Black),
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
            if (index + 1 != ConstantsHelper.OTPCharCount) {
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
    val userDetailsState = viewModel.getUserDetailsStateFlow.collectAsState().value
    when (userDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.Loading
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
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
        }

        RequestStatusEnum.EXCEPTION -> {
            viewModel.snackBarMessageState.value =
                if (userDetailsState.message.isNullOrBlank() || userDetailsState.message == ErrorCodes.NoUserFound) context.getString(
                    R.string.something_went_wrong
                )
                else userDetailsState.message.toString()

            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "OTPInputScreen",
                userDetailsState.message.toString()
            )
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
    val verifyOtpState = viewModel.verifyOtpStateFlow.collectAsState().value

    when (verifyOtpState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.Loading
        }

        RequestStatusEnum.SUCCESS -> {
            if (verifyOtpState.data != null) {
                viewModel.getUserDetails(verifyOtpState.data.uid)
            } else {
                context.showToast(context.getString(R.string.some_error_occurred_please_login_again))
                navigator.popBackStack()
            }
        }

        RequestStatusEnum.EXCEPTION -> {
            viewModel.snackBarMessageState.value =
                if (verifyOtpState.message.isNullOrBlank() || verifyOtpState.message == ErrorCodes.NoUserFound) context.getString(
                    R.string.something_went_wrong
                )
                else verifyOtpState.message.toString()
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "OTPInputScreen",
                verifyOtpState.message.toString()
            )
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
    val resendOtpState = viewModel.resendOtpStateFlow.collectAsState().value
    when (resendOtpState.status) {
        RequestStatusEnum.LOADING -> {
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.Loading
        }

        RequestStatusEnum.SUCCESS -> {
            if (resendOtpState.data?.first == FirebaseConstants.AutoLogin) {
                viewModel.getUserDetails(resendOtpState.data.second)
            } else {
                viewModel.snackBarMessageState.value =
                    stringResource(R.string.otp_sent_successfully)
            }
            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
        }

        RequestStatusEnum.EXCEPTION -> {
            viewModel.snackBarMessageState.value =
                if (resendOtpState.message.isNullOrBlank() || resendOtpState.message == ErrorCodes.NoUserFound) context.getString(
                    R.string.something_went_wrong
                )
                else resendOtpState.message.toString()

            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "OTPInputScreen",
                resendOtpState.message.toString()
            )
        }

        RequestStatusEnum.NONE -> {

        }
    }

}

private fun handleButtonClick(viewModel: OtpInputViewModel, context: Context) {
    if (!viewModel.isValidOTP()) {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.please_enter_valid_otp)
        FunctionHelper.vibrateDevice(context)
    } else {
        viewModel.verifyOTP(viewModel.verificationId)
    }
}


