package com.example.connect.ui.auth.mobile_input

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.ui.common.LoaderButton
import com.example.connect.ui.common.SpacerHeight48
import com.example.connect.ui.common.TopPageSection
import com.example.connect.ui.theme.ConnectTheme
import com.example.connect.utils.FunctionHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MobileNumberInputScreen() {
    val viewModel: MobileNumberInputViewModel = hiltViewModel()
    var numberInputState by remember {
        mutableStateOf(viewModel.userMobileNumber)
    }
    val context = LocalContext.current
    val snackBarHostState = SnackbarHostState()
    val keyboardController = LocalSoftwareKeyboardController.current
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
                OutlinedTextField(value = numberInputState, onValueChange = { updatedValue ->
                    if (updatedValue.length <= 10) {
                        numberInputState = updatedValue
                        viewModel.userMobileNumber = numberInputState
                    } else {
                        viewModel.snackBarMessage.value =
                            context.getString(R.string.mobile_number_can_t_be_greater_than_10_digits)
                        FunctionHelper.vibrateDevice(context)
                        keyboardController?.hide()
                    }
                }, label = {
                    Text(text = stringResource(R.string.please_enter_mobile_number))
                }, leadingIcon = {
                    Text(
                        text = stringResource(R.string._91),
                        color = MaterialTheme.colorScheme.scrim
                    )
                }, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    buttonText = stringResource(id = R.string.get_otp),
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

private fun handleButtonClick(viewModel: MobileNumberInputViewModel, context: Context) {
    if (!viewModel.isValidMobileNumber()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_enter_a_valid_mobile_number)
    } else {
        viewModel.sendOTP()
    }
}


@Preview(showSystemUi = true)
@Composable
fun PreviewSignUpScreen() {
    ConnectTheme {
        Surface {
            MobileNumberInputScreen()
        }
    }
}