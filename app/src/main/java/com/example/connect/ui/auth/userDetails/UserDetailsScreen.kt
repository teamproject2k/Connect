package com.example.connect.ui.auth.userDetails

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.R
import com.example.connect.ui.common.Dropdown
import com.example.connect.ui.common.LoaderButton
import com.example.connect.ui.common.SpacerHeight18
import com.example.connect.ui.common.SpacerHeight48
import com.example.connect.ui.common.TopPageSection
import com.example.connect.utils.FunctionHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserDetailsScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: UserDetailsViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()

    val expanded = remember { viewModel.expanded }
    val selectedItem = remember { viewModel.selectedItem }
    val itemList = remember { viewModel.itemList }

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
                Dropdown(expanded, selectedItem, itemList)
                SpacerHeight18()
                DOBInputTextField(viewModel = viewModel)
                SpacerHeight48()
                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    buttonText = stringResource(id = R.string.create_account),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NameInputTextField(viewModel: UserDetailsViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    OutlinedTextField(
        value = viewModel.userName.value,
        onValueChange = { updatedValue ->
            val pattern = Regex("[0-9!\$@#%^&*()_+{}\\[\\]:;<>,.?~|]")
            if (!updatedValue.contains(pattern) || updatedValue.last() == ' ') {
                if (viewModel.userName.value.isBlank() || viewModel.userName.value.last() != ' ' || updatedValue.last() != ' ')
                    viewModel.userName.value = updatedValue.trimStart()
            } else {
                viewModel.snackBarMessage.value =
                    context.getString(R.string.name_can_t_contain_digits_or_special_characters)
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
            }
        },
        label = {
            Text(text = stringResource(R.string.please_enter_your_full_name))
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
    )
}

@Composable
fun DOBInputTextField(viewModel: UserDetailsViewModel) {

    var selectedDate by remember { mutableStateOf<Date?>(Date()) }
    var dob by remember { viewModel.dob }

    val focusRequester = FocusRequester()
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val onDateSelected = { date: Date ->
        showDatePicker = false
        selectedDate = date
        val formattedDate = SimpleDateFormat("dd  MMM  yyyy", Locale.US).format(date)
        dob = formattedDate
    }

    OutlinedIconButton(
        onClick = {
            showDatePicker = true
            focusRequester.requestFocus()
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Default.DateRange),
                contentDescription = "Dropdown arrow",
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = dob,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    if (showDatePicker) {
        DatePicker(onDateSelected = onDateSelected, viewModel)
    }
}

@Composable
fun DatePicker(
    onDateSelected: (Date) -> Unit,
    viewModel: UserDetailsViewModel
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    calendar.time =
        if (viewModel.dob.value != "Date of Birth") {
            val dateFormat = SimpleDateFormat("dd  MMM  yyyy", Locale.US)
            val date = dateFormat.parse(viewModel.dob.value)
            val timestamp = date!!.time
            Date(timestamp)
        } else Date()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, day, month, year ->
                calendar.set(day, month, year)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    datePickerDialog.show()
}

private fun handleButtonClick(viewModel: UserDetailsViewModel, context: Context) {
    if (!viewModel.isValidName()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_enter_valid_name)
        FunctionHelper.vibrateDevice(context)
    } else if (!viewModel.isGenderSelected()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_select_a_gender)
        FunctionHelper.vibrateDevice(context)
    } else if (!viewModel.isDobSelected()) {
        viewModel.snackBarMessage.value =
            context.getString(R.string.please_select_your_dob)
        FunctionHelper.vibrateDevice(context)
    } else {
        viewModel.userName.value = viewModel.userName.value.trim()
        viewModel.createUserProfile()
    }
}