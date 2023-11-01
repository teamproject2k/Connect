package com.example.connect.ui.auth.mobile_number

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.ui.auth.destinations.OTPScreenDestination
import com.example.connect.ui.common.AppOutlinedTextField
import com.example.connect.ui.common.LoaderButton
import com.example.connect.ui.common.SpacerHeight48
import com.example.connect.ui.common.TopPageSection
import com.example.connect.utils.AuthenticationNavGraph
import com.example.connect.utils.FunctionHelper
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
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TopPageSection(
                stringResource(R.string.welcome),
                stringResource(R.string.let_s_connect),
                stringResource(R.string.log_in)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                MobileInputTextField(viewModel)
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    buttonText = stringResource(id = R.string.get_otp),
                    onClick = {
                        keyboardController?.hide()
//                        handleButtonClick(viewModel, context)
                        navigator.navigate(OTPScreenDestination)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MobileInputTextField(viewModel: MobileNumberInputViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = viewModel.userMobileNumberState.value,
        onValueChange = { updatedValue ->
            if (updatedValue.length <= 10) {
                viewModel.userMobileNumberState.value = updatedValue
            } else {
                viewModel.snackBarMessage.value =
                    context.getString(R.string.mobile_number_can_t_be_greater_than_10_digits)
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
            }
        },
        label = {
            Text(text = stringResource(R.string.please_enter_mobile_number))
        },
        leadingIcon = {
            Text(
                text = stringResource(R.string._91),
                color = MaterialTheme.colorScheme.scrim
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

private fun handleButtonClick(viewModel: MobileNumberInputViewModel, context: Context) {
    if (!viewModel.isValidMobileNumber()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_enter_a_valid_mobile_number)
        FunctionHelper.vibrateDevice(context)
    } else {
        viewModel.sendOTP()
    }
}
