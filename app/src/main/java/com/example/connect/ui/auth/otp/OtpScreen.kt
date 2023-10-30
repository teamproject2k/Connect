package com.example.connect.ui.auth.otp

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.ui.common.LoaderButton
import com.example.connect.ui.common.SpacerHeight18
import com.example.connect.ui.common.SpacerHeight24
import com.example.connect.ui.common.SpacerHeight48
import com.example.connect.ui.common.SpacerWidth6
import com.example.connect.ui.common.TopPageSection
import com.example.connect.ui.theme.ConnectTheme
import com.example.connect.utils.FunctionHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OTPScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: OtpViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
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
                SpacerHeight24()
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
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(viewModel, context)
                    }
                )
            }
        }
    }
    LaunchedEffect(key1 = viewModel.snackBarMessage.value) {
        if (viewModel.snackBarMessage.value.isNotBlank()) {
            snackBarHostState.showSnackbar(
                viewModel.snackBarMessage.value,
                duration = SnackbarDuration.Short
            )
            viewModel.snackBarMessage.value = ""
        }
    }
}

@Composable
fun OTPTTimer(viewModel: OtpViewModel) {
    val showTimer by remember {
        viewModel.showTimerState
    }
    LaunchedEffect(key1 = showTimer) {
        if (showTimer) {
            viewModel.startTimer()
        }
    }
    if (showTimer) {
        Text(
            text = stringResource(
                id = R.string.digit_string_placeholder,
                viewModel.timeLeftState.value,
                if (viewModel.timeLeftState.value > 1) {
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
                viewModel.resendOtp()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPField(viewModel: OtpViewModel) {
    var requestFocusPos by remember {
        mutableStateOf(0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            Row(
                modifier = Modifier
                    .weight(1f)
            ) {
                OutlinedTextField(
                    value = viewModel.otpState.value[index],
                    onValueChange = { updatedValue ->
                        if (updatedValue.length == 1) {
                            viewModel.otpState.value[index] = updatedValue
                        } else if (updatedValue.isBlank()) {
                            viewModel.otpState.value[index] = ""
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                )
            }
            if (index + 1 != 6) {
                SpacerWidth6()
            }
        }
    }

}

private fun handleButtonClick(viewModel: OtpViewModel, context: Context) {
    if (!viewModel.isValidOTP()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_enter_valid_otp)
        FunctionHelper.vibrateDevice(context)
    } else {
        viewModel.verifyOTP()
    }
}


@Preview
@Composable
fun PreviewOTPScreen() {
    ConnectTheme {
        Surface {
            OTPScreen()
        }
    }
}
